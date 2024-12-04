
<h1 align="center">api-no-jutsu</h1>


## 📝 Tabla de contenidos

1. [Introduccion](#introduccion)
1. [Api](#api)
3. [Autor](#autor)


## <a name="introduccion"></a> 🧐 Introduccion

He creado una api para todas las paginas de anime en castellano llamado `api-no-jutsu`.Es un desarrollo en java basado en SpringBoot 3 y va de la mano del scrapper api-no-jutsu-scrapper, de un bot de telegram y una base de datos MySQL (algunos de estos repositorios estan ocultos de momento).
Este proyecto nace por la necesidad de tener un api comun para todas las paginas de anime y poder mostrar las novedades en telegram.

## <a name="api"></a> 🖥️ Api

### **Login**
Permite a un usuario iniciar sesión en AnimeFLV utilizando sus credenciales.

#### Endpoint:
```http
POST /api/animeflv/login
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `username` | `string` | **Required**. Usuario para iniciar sesión. |
| `password` | `string` | **Required**. Contraseña del usuario. |

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `status` | `string` | Estado de la operación (success o error). |
| `username` | `string` | Nombre de usuario. |
| `message` | `string` | Mensaje de respuesta. |



### **Obtener Novedades de Episodios**
Devuelve una lista de los episodios más recientes añadidos en AnimeFLV.

#### Endpoint:
```http
GET /api/animeflv/novedades-episodios
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `N/A` | `N/A` | N/A |

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `titulo` | `string` | Título del episodio.|
| `anime` | `string` | Nombre del anime. |
| `fecha` | `string` | Fecha de lanzamiento. |
| `url` | `string` | URL para más información. |

### **Obtener Novedades de Animes**
Devuelve una lista de los animes más recientes añadidos en AnimeFLV.

#### Endpoint:
```http
GET /api/animeflv/novedades-animes
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `N/A` | `N/A` | N/A |

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `titulo` | `string` | Título del anime. |
| `url` | `string` | 	URL para más detalles. |

### **Obtener Información de un Anime**
Devuelve información detallada sobre un anime específico.

#### Endpoint:
```http
GET /api/animeflv/obtener-anime
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `anime` | `string` | Nombre del anime a buscar.|

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `titulo` | `string` | Título del anime. |
| `descripcion` | `string` | Descripción o sinopsis del anime. |
| `genero` | `string` | Género del anime. |
| `fecha_estreno` | `string` | Fecha de estreno del anime. |
| `estado` | `string` | Estado de emisión (En emisión, Finalizado). |


### **Obtener URLs de Visualización de un Episodio**
Devuelve los enlaces de visualización para un episodio específico.

#### Endpoint:
```http
GET /api/animeflv/obtener-url-visualizacion
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `animeEpisode` | `string` | **required.** Nombre del episodio a buscar. |

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `titulo` | `string` | Título del episodio. |
| `urls` | `array` | Lista de URLs de visualización. |

### **Logout**
Cierra la sesión de un usuario en AnimeFLV.

#### Endpoint:
```http
POST /api/animeflv/logout
```
#### Request:

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `username` | `string` |**required.** Usuario para cerrar sesión. |

#### Response

| Parameter | Type     | Description                |
| :-------- | :------- | :------------------------- |
| `status` | `string` | Estado de la operación (success o error). |
| `username` | `string` | Nombre de usuario. |
| `message` | `string` | Mensaje de respuesta. |

## <a name="autor"></a> ✨ Autor
La api esta complemamente desarrollada por @yraedry