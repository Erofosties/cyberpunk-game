import { CommonModule } from '@angular/common';
import { HttpClient, HttpHeaders, HttpParams } from '@angular/common/http';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

type Section = 'auth' | 'usuarios' | 'colonias' | 'mapa' | 'edificios' | 'personajes';

@Component({
  selector: 'app-root',
  imports: [CommonModule, FormsModule],
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App {
  private readonly http = inject(HttpClient);

  readonly title = 'Cyberpunk Command';
  readonly section = signal<Section>('auth');

  apiBase = 'http://localhost:8080';
  readonly token = signal(localStorage.getItem('cyberpunk_token') ?? '');
  readonly usuarioIdActual = signal<number | null>(
    Number(localStorage.getItem('cyberpunk_user_id') ?? '') || null
  );

  readonly status = signal('Listo para pruebas');
  readonly loading = signal(false);
  readonly lastResponse = signal('');
  readonly history = signal<string[]>([]);

  readonly authRegister = {
    username: '',
    password: ''
  };

  readonly authLogin = {
    username: '',
    password: ''
  };

  readonly usuariosForm = {
    id: 1,
    page: 0,
    size: 20
  };

  readonly coloniaForm = {
    id: 1,
    x: 0,
    y: 0
  };

  readonly mapaForm = {
    usuarioId: 1,
    x: 0,
    y: 0,
    centerX: 0,
    centerY: 0,
    radius: 4
  };

  readonly edificioForm = {
    coloniaId: 1,
    tipoEdificio: 'MINA_NEOCROMO',
    sectorId: 1,
    personajeId: 1,
    construccionId: 1,
    activo: true
  };

  readonly personajeForm = {
    coloniaId: 1,
    nombre: 'Neo',
    fuerza: 2,
    destreza: 2,
    resistencia: 1,
    hackeo: 1,
    mineria: 2,
    agricultura: 2,
    ciencia: 1,
    ingenieria: 1,
    usuarioId: 1,
    guerreroIds: '1,2',
    x: 0,
    y: 0,
    personajeId: 1,
    guerreroId: 1
  };

  readonly mapCells = computed(() => {
    const cells: { x: number; y: number; isCenter: boolean }[] = [];
    const r = this.mapaForm.radius;
    for (let yy = this.mapaForm.centerY + r; yy >= this.mapaForm.centerY - r; yy--) {
      for (let xx = this.mapaForm.centerX - r; xx <= this.mapaForm.centerX + r; xx++) {
        cells.push({ x: xx, y: yy, isCenter: xx === this.mapaForm.centerX && yy === this.mapaForm.centerY });
      }
    }
    return cells;
  });

  setSection(next: Section): void {
    this.section.set(next);
  }

  clearLog(): void {
    this.history.set([]);
    this.lastResponse.set('');
  }

  private authHeaders(): HttpHeaders {
    const current = this.token().trim();
    return current ? new HttpHeaders({ Authorization: `Bearer ${current}` }) : new HttpHeaders();
  }

  private setAuthFromResponse(response: unknown): void {
    const value = response as { token?: string; usuarioId?: number };
    if (!value.token) {
      return;
    }

    this.token.set(value.token);
    localStorage.setItem('cyberpunk_token', value.token);

    if (typeof value.usuarioId === 'number') {
      this.usuarioIdActual.set(value.usuarioId);
      localStorage.setItem('cyberpunk_user_id', String(value.usuarioId));
      this.mapaForm.usuarioId = value.usuarioId;
      this.personajeForm.usuarioId = value.usuarioId;
      this.coloniaForm.id = value.usuarioId;
    }
  }

  logout(): void {
    this.token.set('');
    this.usuarioIdActual.set(null);
    localStorage.removeItem('cyberpunk_token');
    localStorage.removeItem('cyberpunk_user_id');
    this.status.set('Sesion cerrada');
  }

  private request(
    method: 'GET' | 'POST',
    path: string,
    body?: unknown,
    query?: Record<string, string | number | boolean>
  ): void {
    const base = this.apiBase.replace(/\/$/, '');
    const url = `${base}${path}`;
    let params = new HttpParams();

    if (query) {
      for (const [key, value] of Object.entries(query)) {
        params = params.set(key, String(value));
      }
    }

    this.loading.set(true);
    this.status.set(`Ejecutando ${method} ${path}...`);

    const options = {
      headers: this.authHeaders(),
      params
    };

    const obs = method === 'GET'
      ? this.http.get(url, { ...options, responseType: 'text' as const })
      : this.http.post(url, body ?? {}, { ...options, responseType: 'text' as const });

    obs.subscribe({
      next: (responseText) => {
        const pretty = this.prettyText(responseText);
        this.lastResponse.set(pretty);
        this.history.set([`OK ${method} ${path}`, ...this.history()].slice(0, 20));
        this.status.set(`OK ${method} ${path}`);
        this.loading.set(false);

        if (path.startsWith('/auth/')) {
          try {
            const parsed = JSON.parse(responseText) as unknown;
            this.setAuthFromResponse(parsed);
          } catch {
            // ignore
          }
        }
      },
      error: (error) => {
        const payload = error?.error ?? error?.message ?? error;
        const formatted = typeof payload === 'string' ? this.prettyText(payload) : this.prettyJson(payload);
        this.lastResponse.set(formatted);
        this.history.set([`ERROR ${method} ${path}`, ...this.history()].slice(0, 20));
        this.status.set(`Error en ${method} ${path}`);
        this.loading.set(false);
      }
    });
  }

  private prettyText(text: string): string {
    try {
      const parsed = JSON.parse(text);
      return this.prettyJson(parsed);
    } catch {
      return text;
    }
  }

  private prettyJson(data: unknown): string {
    return JSON.stringify(data, null, 2);
  }

  register(): void {
    this.request('POST', '/auth/register', this.authRegister);
  }

  login(): void {
    this.request('POST', '/auth/login', this.authLogin);
  }

  getUsuarios(): void {
    this.request('GET', '/usuarios', undefined, {
      page: this.usuariosForm.page,
      size: this.usuariosForm.size
    });
  }

  getUsuarioById(): void {
    this.request('GET', `/usuarios/${this.usuariosForm.id}`);
  }

  getColonia(): void {
    this.request('GET', `/colonias/${this.coloniaForm.id}`);
  }

  desplegarNave(): void {
    this.request('POST', `/colonias/${this.coloniaForm.id}/desplegar`, {
      x: this.coloniaForm.x,
      y: this.coloniaForm.y
    });
  }

  getMapaVisible(): void {
    this.request('GET', `/map/${this.mapaForm.usuarioId}`);
  }

  getSectorDetalle(): void {
    this.request('GET', '/map/sector', undefined, {
      usuarioId: this.mapaForm.usuarioId,
      x: this.mapaForm.x,
      y: this.mapaForm.y
    });
  }

  clickSector(x: number, y: number): void {
    this.mapaForm.x = x;
    this.mapaForm.y = y;
    this.getSectorDetalle();
  }

  construirEdificio(): void {
    this.request('POST', '/edificios/construir', {
      coloniaId: this.edificioForm.coloniaId,
      tipoEdificio: this.edificioForm.tipoEdificio,
      sectorId: this.edificioForm.sectorId
    });
  }

  asignarConstruccion(): void {
    this.request('POST', '/edificios/asignar-construccion', {
      personajeId: this.edificioForm.personajeId,
      construccionId: this.edificioForm.construccionId
    });
  }

  asignarTrabajo(): void {
    this.request('POST', '/edificios/asignar-trabajo', {
      personajeId: this.edificioForm.personajeId,
      sectorId: this.edificioForm.sectorId
    });
  }

  desasignarTrabajo(): void {
    this.request('POST', `/edificios/desasignar-trabajo/${this.edificioForm.personajeId}`, {});
  }

  estadoGeneradorNeon(): void {
    this.request('POST', '/edificios/generador-neon/estado', {
      coloniaId: this.edificioForm.coloniaId,
      sectorId: this.edificioForm.sectorId,
      activo: this.edificioForm.activo
    });
  }

  verConstrucciones(): void {
    this.request('GET', `/edificios/construcciones/${this.edificioForm.coloniaId}`);
  }

  crearPersonaje(): void {
    this.request('POST', '/personajes', {
      coloniaId: this.personajeForm.coloniaId,
      nombre: this.personajeForm.nombre
    });
  }

  crearTrabajador(): void {
    this.request('POST', '/personajes/trabajador', {
      coloniaId: this.personajeForm.coloniaId,
      nombre: this.personajeForm.nombre,
      mineria: this.personajeForm.mineria,
      agricultura: this.personajeForm.agricultura,
      ciencia: this.personajeForm.ciencia,
      ingenieria: this.personajeForm.ingenieria
    });
  }

  crearGuerrero(): void {
    this.request('POST', '/personajes/guerrero', {
      coloniaId: this.personajeForm.coloniaId,
      nombre: this.personajeForm.nombre,
      fuerza: this.personajeForm.fuerza,
      destreza: this.personajeForm.destreza,
      resistencia: this.personajeForm.resistencia,
      hackeo: this.personajeForm.hackeo
    });
  }

  desplegarGuerreros(): void {
    const ids = this.personajeForm.guerreroIds
      .split(',')
      .map((id) => Number(id.trim()))
      .filter((id) => !Number.isNaN(id));

    this.request('POST', '/personajes/guerreros/desplegar', {
      usuarioId: this.personajeForm.usuarioId,
      guerreroIds: ids,
      x: this.personajeForm.x,
      y: this.personajeForm.y
    });
  }

  retirarGuerrero(): void {
    this.request('POST', `/personajes/guerreros/${this.personajeForm.guerreroId}/retirar`, {});
  }

  usarNanocura(): void {
    this.request('POST', `/personajes/${this.personajeForm.personajeId}/nanocura`, {
      coloniaId: this.personajeForm.coloniaId
    });
  }

  usarFlorsomnio(): void {
    this.request('POST', `/personajes/${this.personajeForm.personajeId}/florsomnio`, {
      coloniaId: this.personajeForm.coloniaId
    });
  }
}
