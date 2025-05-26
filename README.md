# 📲 NeoFichaje

**NeoFichaje** es una aplicación Android desarrollada para facilitar el control horario, la gestión de empleados en empresas pequeñas y medianas.

## 🛠 Tecnologías utilizadas

- **Android Studio** – Entorno de desarrollo
- **Kotlin** – Lenguaje de programación
- **XML** – Diseño de interfaces
- **Firebase Authentication** – Registro y login de usuarios
- **Firestore** – Base de datos en la nube (NoSQL)
- **Firebase Cloud Storage** – Almacenamiento de archivos (PDF de contratos y nóminas)
- **Google Sign-In** – Inicio de sesión con cuenta Google
- **MaterialCalendarView** – Componente visual de calendario para seleccionar fechas

## 👥 Roles de usuario

- **Administrador / Empresario**: puede registrar empleados, subir contratos y nóminas, gestionar asistencia y aprobar solicitudes.
- **Empleado / Técnico**: puede fichar entradas y salidas, solicitar vacaciones y permisos, y ver sus documentos personales.

## 🧩 Funcionalidades

- 📅 Control horario con geolocalización
- ✍️ Registro y login seguro (con correo y Google)
- 👤 Gestión de perfil
- 🧾 Visualización y descarga de contratos y nóminas
- 🌴 Solicitudes de vacaciones y permisos
- 🔔 Notificaciones internas para empleados y administradores

## 🔄 Flujo general

1. El usuario se registra o inicia sesión  con su correo o cuenta de Google.
2. Dependiendo de su rol (técnico o empresario), accede a su panel personalizado.
3. El técnico puede fichar entradas y salidas, solicitar vacaciones y permisos.
4. El empresario puede gestionar solicitudes, aprobándolas o rechazándolas.
5. El empresario también puede subir documentos PDF (como contratos o nóminas), y el técnico puede visualizarlos.

## 📂 Organización del código

- /app – Lógica principal de la app (actividades, fragments, modelos)
- /gradle – Configuraciones del proyecto
- firebase.json – Configuración de Firebase

## 📈 Progreso

En este apartado se puede consultar todo el historial de trabajo realizado en el proyecto: desde los primeros avances hasta los ajustes finales.
Puedes acceder a los [commits](https://github.com/KarinaRojasDev/proyecto-neo-fichaje/commits/main) en GitHub para ver paso a paso cómo fue evolucionando el desarrollo.

## 👩‍💻 Autora

Desarrollado por **Karina Rojas** como proyecto final del segundo curso de Desarrollo de Aplicaciones Multiplataforma (DAM).
