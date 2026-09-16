# language: es
Característica: Validación y errores de tareas
  Como consumidor de la API
  Quiero recibir errores consistentes
  Para poder corregir solicitudes inválidas

  Escenario: Rechazar una tarea sin título
    Dado que no hay tareas
    Cuando creo una tarea con título " " y descripción "Sin título válido"
    Entonces la respuesta tiene estado 400
    Y el código de error es "VALIDATION_ERROR"
    Y la respuesta contiene una infracción para el campo "title"

  Escenario: Informar que una tarea no existe
    Dado que no hay tareas
    Cuando consulto la tarea con id 9999
    Entonces la respuesta tiene estado 404
    Y el código de error es "TASK_NOT_FOUND"