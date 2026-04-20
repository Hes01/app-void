<div align="center">

# VOID Launcher

![Android](https://img.shields.io/badge/Android-4.1%2B-3DDC84?style=flat-square&logo=android&logoColor=white)
![Java](https://img.shields.io/badge/Java-puro-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Tamaño](https://img.shields.io/badge/tamaño-~92KB-blue?style=flat-square)
![Dependencias](https://img.shields.io/badge/dependencias-cero-success?style=flat-square)
![Licencia](https://img.shields.io/github/license/Hes01/app-void?style=flat-square)
![Estrellas](https://img.shields.io/github/stars/Hes01/app-void?style=flat-square)

![Visitas](https://visitor-badge.laobi.icu/badge?page_id=Hes01.app-void&left_color=555555&right_color=000000&left_text=visitas)

Un launcher Pantalla oscura + buscador tipo terminal.

</div>

---

<p align="center">
  <img src="public/bg_reloj.png" width="30%" />
  <img src="public/bg_buscador.png" width="30%" />
  <img src="public/bg_alias.png" width="30%" />
</p>

---

## Por qué existe

Nació de la necesidad de buscar algo ligero, sencillo y rápido sin ningún tipo de permiso que minimice la fricción en mi caso.

## Cómo funciona

Tocas cualquier parte de la pantalla y aparece el buscador. Escribes las primeras letras de la app y listo. Si solo hay una coincidencia, la abre directo sin que tengas que confirmar nada.

La búsqueda también aprende. Si siempre abres Spotify a las 7am, a esa hora Spotify aparece de primero sin escribir ni una letra. Todo pasa en el teléfono, sin servidores, sin internet, no se recopila información de ningún tipo.

## Comandos

| Comando | Qué hace |
|---|---|
| `.all` | Lista todas las apps instaladas |
| `.void` | Abre los ajustes (poner alias a tus apps) |

## Lo que no tiene

- Iconos
- Widgets
- Animaciones
- Notificaciones
- Conexión a internet
- Publicidad
- Rastreo de ningún tipo

## Números

- **1 permiso**: leer apps instaladas (Android 11+ lo exige)
- **~92KB** el APK release con ProGuard
- **Android 4.1+** compatible

## Arquitectura

```
app/
├── LauncherActivity.java    — pantalla principal, reloj, receptor de eventos
├── GestureView.java         — captura toques
├── QuickSearchDialog.java   — buscador y comandos
├── ContextualApps.java      — aprende qué apps usas y cuándo
├── SettingsActivity.java    — ajustes
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

## Ecosistema VOID (próximamente)

VOID está pensado como un sistema modular. Cada pieza es una app independiente de menos de 100KB realizado por necesidad y para optimizar recursos.

- **VOID** — el launcher (este repo)
- **VOID Note** — bloc de notas minimalista (próximamente)

---

<div align="center">
Hecho para gente que quiere entrar, hacer lo que tiene que hacer y ya.
</div>
