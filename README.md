<div align="center">

# VOID Launcher

![Android](https://img.shields.io/badge/Android-4.1%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-puro-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Tamaño](https://img.shields.io/badge/tamaño-~26KB-blue?style=flat-square)
![Dependencias](https://img.shields.io/badge/dependencias-cero-success?style=flat-square)
![Licencia](https://img.shields.io/github/license/Hes01/app-void?style=flat-square)
![Estrellas](https://img.shields.io/github/stars/Hes01/app-void?style=flat-square)

![Visitas](https://hits.seeyoufarm.com/api/count/incr/badge.svg?url=https%3A%2F%2Fgithub.com%2FHes01%2Fapp-void&count_bg=%23000000&title_bg=%23555555&icon=github.svg&icon_color=%23FFFFFF&title=visitas&edge_flat=true)

Un launcher que no te pide atención. Pantalla negra, buscador tipo terminal y ya.

</div>

---

<p align="center">
  <img src="public/bg_reloj.png" width="30%" />
  <img src="public/bg_buscador.png" width="30%" />
  <img src="public/bg_alias.png" width="30%" />
</p>

---

## Por qué existe

Me cansé de los iconos, los colores y las notificaciones gritándome en la cara cada vez que desbloqueaba el teléfono. VOID no tiene nada de eso. Es una pantalla negra, un reloj con un círculo fino y silencio.

Si quieres abrir algo, tocas y escribes. Como una terminal, pero desde tu teléfono.

## Cómo funciona

Tocas cualquier parte de la pantalla y aparece el buscador. Escribes las primeras letras de la app y listo. Si solo hay una coincidencia, la abre directo sin que tengas que confirmar nada.

La búsqueda también aprende. Si siempre abres Spotify a las 7am, a esa hora Spotify aparece de primero sin escribir ni una letra. Todo pasa en el teléfono, sin servidores, sin internet.

## Comandos

| Comando | Qué hace |
|---|---|
| `/all` | Lista todas las apps instaladas |
| `/void` | Abre los ajustes (modo morse) |
| `/nn` | Abre VOID Note si está instalado |
| `/nn titulo` | Abre VOID Note con ese título listo |
| `/nn -l` | Lista tus notas |
| `/nn -rm 1` | Borra la nota con id 1 |
| `/<alias> -d` | Desinstala la app con ese alias |

## Lo que no tiene (a propósito)

- Iconos
- Widgets
- Animaciones
- Notificaciones
- Conexión a internet
- Publicidad
- Rastreo de ningún tipo

## Números

- **673 líneas** de código en total
- **6 archivos** Java
- **1 permiso**: leer apps instaladas (Android 11+ lo exige)
- **~26KB** el APK release con ProGuard
- **Android 4.1+** compatible

## Arquitectura

```
app/
├── LauncherActivity.java    — pantalla principal, reloj, receptor de eventos
├── GestureView.java         — captura toques
├── QuickSearchDialog.java   — buscador y comandos
├── ContextualApps.java      — aprende qué apps usas y cuándo
├── SettingsActivity.java    — ajustes en morse (sí, en serio)
└── AppLauncher.java         — lanza apps por packageName
```

## Instalar

Descarga el APK desde [Releases](https://github.com/Hes01/app-void/releases) e instálalo. Necesitas permitir instalación desde fuentes desconocidas si no viene de la Play Store.

O clona y compila tú mismo:

```bash
git clone https://github.com/Hes01/app-void.git
cd app-void
./gradlew assembleRelease
```

## Ecosistema VOID

VOID está pensado como un sistema modular. Cada pieza es una app independiente de menos de 100KB.

- **VOID** — el launcher (este repo)
- **VOID Note** — bloc de notas minimalista (próximamente)

---

<div align="center">
Hecho para gente que quiere entrar, hacer lo que tiene que hacer y salir.
</div>
