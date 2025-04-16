# Курс по кафке

## Зачем нужна кафка?
1. Сообщение сразу множеству получателей
2. Нет потери сообщений, если получатель недоступен
3. Добавление получателей не требует доработки отравителя

## Event-Driven Architecture
1. Асинхронное взаимодействие
2. Слабые связи
3. Гибкость изменений
4. Легкость масштабирования
5. При восстановлении concumer может обработать пропущенные сообщения
6. Producer не ждет обработки не знает про то, кто обрабатывает

## Понятия в кафке
**Broker** - это сервер, который принимает сообщения от продюсера и сохраняет сообщение на диске.
Он принимает сообщения от производителей, хранит их и передает потребителям.

Брокер может быть как leader, так и follower. 
**Leader** - сервер, который обрабатывает все сообщения
**Follower** - сервер, который лишь дублирует сообщения и вступает в работу, когда лидер упал.
Брокер может быть одновременно как leader, так и follower.

**Topic** - категория или канал, в котором данные организуются или хранятся в kafka. 
Он представляет собой название, под которым данные публикуются производителями и потребляются потребителями.

**Партиция** — это физическая единица внутри топика, которая разбивает
топик на несколько логически независимых частей. Топик используется
для горизонтального масштабирования.

## Структура сообщения
1. Key (null, json, string) - массив байт
2. Event (null, json, string) - массив байт
3. Timestamp
4. Header

## Запуск сервера
```
.\kafka-storage.bat random-uuid - генерим uuid для кластера
.\kafka-storage.bat format -t 'сгенерированный uuid' -c ../../config/kraft/server.properties - форматируем логи
.\kafka-server-start.bat ../../config/kraft/server.properties - запуск сервера кафки
```

### Кафка cluster. Конфигурация и запуск
1. Создать server-1.properties, server-2.properties, server-3.properties
2. Заполнить данными следующие настройки:
```
node.id=
listeners=
advertised.listeners=
log.dirs=
```
3. Выполнить следующие команды:
```
.\kafka-storage.bat random-uuid - генерим uuid для кластера
.\kafka-storage.bat format -t 'сгенерированный uuid' -c ../../config/kraft/server-*.properties - форматируем логи
.\kafka-server-start.bat ../../config/kraft/server-*.properties - запуск сервера кафки
.\kafka-server-stop.bat - остановить сервер кафки
```

## Создание, просмотр и удаление топика
```
.\kafka-topics.bat --create --topic payment-created-events-topic --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092,localhost:9094 - создание топика
.\kafka-topics.bat --list --bootstrap-server localhost:9092,localhost:9094 - просмотр всех топиков
.\kafka-topics.bat --describe --bootstrap-server localhost:9092,localhost:9094- просмотр детальной информации по топикам
.\kafka-topics.bat --delete --topic payment-created-events-topic --partitions 3 --replication-factor 3 --bootstrap-server localhost:9092,localhost:9094 - удаление топика
```
Если возникла ошибка, можно почистить папку tmp, где пишутся логи




