/**
 * Sonidos sintetizados con Web Audio (sin archivos externos).
 * Singleton perezoso: el AudioContext se crea en el primer gesto del usuario.
 */
type Wave = OscillatorType;

class SoundEngine {
  private ctx: AudioContext | null = null;
  private muted = false;

  constructor() {
    try {
      this.muted = localStorage.getItem('aurum.muted') === '1';
    } catch {
      /* sin almacenamiento */
    }
  }

  isMuted(): boolean {
    return this.muted;
  }

  setMuted(muted: boolean): void {
    this.muted = muted;
    try {
      localStorage.setItem('aurum.muted', muted ? '1' : '0');
    } catch {
      /* sin almacenamiento */
    }
  }

  private context(): AudioContext | null {
    if (this.muted) return null;
    if (!this.ctx) {
      const Ctor = window.AudioContext ?? (window as unknown as { webkitAudioContext?: typeof AudioContext }).webkitAudioContext;
      if (!Ctor) return null;
      this.ctx = new Ctor();
    }
    if (this.ctx.state === 'suspended') void this.ctx.resume();
    return this.ctx;
  }

  private tone(freq: number, duration: number, { wave = 'sine' as Wave, gain = 0.08, delay = 0, slideTo = 0 } = {}) {
    const ctx = this.context();
    if (!ctx) return;
    const start = ctx.currentTime + delay;
    const osc = ctx.createOscillator();
    const amp = ctx.createGain();
    osc.type = wave;
    osc.frequency.setValueAtTime(freq, start);
    if (slideTo) osc.frequency.exponentialRampToValueAtTime(slideTo, start + duration);
    amp.gain.setValueAtTime(0.0001, start);
    amp.gain.exponentialRampToValueAtTime(gain, start + 0.01);
    amp.gain.exponentialRampToValueAtTime(0.0001, start + duration);
    osc.connect(amp).connect(ctx.destination);
    osc.start(start);
    osc.stop(start + duration + 0.02);
  }

  chip() {
    this.tone(1800, 0.05, { wave: 'triangle', gain: 0.05 });
    this.tone(2600, 0.04, { wave: 'triangle', gain: 0.03, delay: 0.02 });
  }

  tick() {
    this.tone(900, 0.03, { wave: 'square', gain: 0.015 });
  }

  spin() {
    this.tone(180, 0.35, { wave: 'sawtooth', gain: 0.03, slideTo: 420 });
  }

  stop() {
    this.tone(140, 0.12, { wave: 'triangle', gain: 0.09 });
  }

  lose() {
    this.tone(320, 0.25, { wave: 'sine', gain: 0.04, slideTo: 200 });
  }

  /** Carta deslizándose sobre el paño. */
  deal() {
    this.tone(1400, 0.06, { wave: 'triangle', gain: 0.035, slideTo: 600 });
  }

  /** Gema encontrada: dos notas brillantes. */
  gem() {
    this.tone(1318.5, 0.12, { wave: 'sine', gain: 0.06 });
    this.tone(1975.5, 0.18, { wave: 'sine', gain: 0.05, delay: 0.06 });
  }

  /** Explosión grave para minas y crash. */
  boom() {
    this.tone(160, 0.5, { wave: 'sawtooth', gain: 0.09, slideTo: 40 });
    this.tone(90, 0.6, { wave: 'square', gain: 0.04, slideTo: 30, delay: 0.02 });
  }

  /** Arpegio ascendente; más notas cuanto mayor es el premio. */
  win(level: 1 | 2 | 3 = 1) {
    const notes = [523.25, 659.25, 783.99, 1046.5, 1318.5, 1568];
    const count = 2 + level * 1.4;
    for (let i = 0; i < count; i++) {
      this.tone(notes[i % notes.length], 0.22, { wave: 'triangle', gain: 0.07, delay: i * 0.08 });
    }
  }
}

export const sound = new SoundEngine();
