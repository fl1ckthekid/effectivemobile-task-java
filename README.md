# Система управления банковскими картами

---

Данный проект использует Docker Compose для запуска Spring Boot приложения и базы данных PostgreSQL в отдельных контейнерах.

## Предварительные требования

* Установленный [Docker](https://docs.docker.com/get-docker/)
* Установленный [Docker Compose](https://docs.docker.com/compose/install/)

## Инструкция по запуску

1. **Склонируйте репозиторий и перейдите в директорию проекта:**

```bash
git clone <URL_репозитория>
cd <директория_проекта>
```

2. **Соберите Docker-образ приложения:**

```bash
docker-compose build
```

3. **Запустите контейнеры с помощью Docker Compose:**

```bash
docker-compose up
```

Эта команда поднимет два контейнера:

* **db** — контейнер с PostgreSQL (порт 5432)
* **app** — контейнер со Spring Boot приложением (порт 8080)

## Остановка контейнеров

Чтобы остановить и удалить контейнеры, выполните:

```bash
docker-compose down
```

---