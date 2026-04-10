# Activities — Android las instancia por nombre desde el Manifest
-keep public class com.voidlauncher.ui.** extends android.app.Activity

# GestureMapping — serializado a JSON a mano, los campos deben conservar su nombre
-keep class com.voidlauncher.data.GestureMapping { *; }

# Interfaces de callback internas
-keep interface com.voidlauncher.ui.GestureView$Listener { *; }
