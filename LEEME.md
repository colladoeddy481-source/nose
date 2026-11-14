# Rick Prime Regeneration (Fabric 1.21.1)

Comandos: /regen (inicia la animacion) y /regen reset (devuelve tu skin).
Tambien se activa solo al recibir un golpe mortal (max. 12 veces, ver RegenManager.MAX_DEATH_REGENS).

Skin: reemplaza src/main/resources/assets/rickregen/textures/entity/rick_prime.png por tu skin 64x64 (modelo de brazos anchos).

Compilar: ./gradlew build  (JDK 21). El .jar queda en build/libs/.
Si no hay wrapper: genera una plantilla en fabricmc.net/develop (1.21.1, Mojang mappings) y copia aqui la carpeta src/.

## Obtener el .jar sin instalar nada (GitHub Actions)
1. Crea un repositorio en GitHub y sube todo el contenido de esta carpeta (incluida .github).
2. Ve a la pestana "Actions" y abre la ejecucion "Compilar mod".
3. Al terminar, descarga el artefacto "rickregen-jar" (un zip con el .jar dentro).
Si falla, abre el paso "Compilar" y copia el error.
