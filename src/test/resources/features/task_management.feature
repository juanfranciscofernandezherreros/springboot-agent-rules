# language: es
Característica: Gestión de tareas
  Como usuario de la API
  Quiero administrar tareas
  Para organizar el trabajo pendiente

  Escenario: Crear y consultar una tarea
    Dado que no hay tareas
    Cuando creo una tarea con título "Preparar API" y descripción "Completar el backend"
    Entonces la respuesta tiene estado 201
    Y la respuesta contiene el título "Preparar API"
    Cuando consulto la tarea creada
    Entonces la respuesta tiene estado 200
    Y la respuesta contiene el título "Preparar API"

  Escenario: Buscar tareas con paginación
    Dado que existe una tarea con título "Preparar API"
    Y que existe una tarea con título "Revisar documentación"
    Cuando busco tareas por el título "API" con tamaño de página 10
    Entonces la respuesta tiene estado 200
    Y la búsqueda devuelve 1 tarea

  Escenario: Modificar y eliminar una tarea
    Dado que existe una tarea con título "Publicar versión"
    Cuando marco la tarea creada como completada
    Entonces la respuesta tiene estado 200
    Y la respuesta indica que está completada
    Cuando elimino la tarea creada
    Entonces la respuesta tiene estado 204
    Cuando consulto la tarea creada
    Entonces la respuesta tiene estado 404
    Y el código de error es "TASK_NOT_FOUND"