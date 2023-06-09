### moveRecords   

Этот скрипт предназначен для перемещения записей разговоров записанных ip АТС Asterisk на другой раздел.  
Бывают ситуации, когда записи разговоров необходимо хранить на удаленном сервере, директория которого примонтирована с помощью nfs или cifs.   
По умолчанию записи разговоров на сервере Asterisk хранятся в директории /var/spool/asterisk/monitor.
Сразу писать файлы на сетевой диск не рекомендуется, если призойдет сетевой сбой или удаленный сервер хранения записей выйдет из строя, в таком случае записи будут потеряны. 
Надежный вариат будет если записи разговоров перемещать после того, как файл запишется на локальный диск.   
Здесь представлен скрипт ***moveRecords.jar*** который размещен в каталоге ***/target/*** который забирает записи разговоров с гокального диска и безопасно перемещает их на сетевой диск. Ниже будет описание его работы   
Скрипт умеет работать с логированием в 2 режимах:   
1. По умолчанию логи пишутся в вайл на сервере Asterisk в /var/log/moveRecords/moveRecords.log. Ротация каждые 10 дней.
2. Логи пишутся в файл как в первом пункте плюс критические уведомления можно отправлять в группу телеграмм   

После перемещения файла в другое место на месте старого файла создается symlick на новый файл, таким образом файлы остаются доступными к скачиванию и прослушиванию через стандартный CDR Report панели FreePBX.

Перед использованием скприпта необходимо убедится, что на сервере установлена java 17 версии, для этого необходимо выполнить команду:
```java -version```
Если версия не 17, или получили ответ ```-bash: java: command not found```
С сайта Oracle скачиваем rpm пакет и устанавливаем его:
```yum localinstall <путь к rpm пакету>```

#### Примеры использования
Предположим, что сетевой диск примонтирован в директорию ```/mnt/monitor```
Тогда для копирования записей без уведомлений в телеграмм необходимо выполнить следующую команду:
```java -jar moveRecords.jar srcdir=/var/spool/asterisk/monitor dstdir=/mnt/monitor```

Если у сервера есть выход в интернет и требуются уведомления в телеграмм, для этого необходимо создать бота в телеграмме, создать группу в телеграмме, добавить в группу нужных участников и только что созданного бота.
Далее необходимо узнать bot_id (id бота) и chat_id (id созданного чата). Как это все сделать можно найти в интернете в свободном доступе.   
После того, как подготовили телеграмм группу с ботом на сервере Asterisk необходимо выполнить команду:
```java -jar moveRecords.jar srcdir=/var/spool/asterisk/monitor dstdir=/mnt/monitor botid=bot1773336691:BBBBClxq6Ewry_o5cWfdRrA4dVLITut2E12 chatid=-155711111  @masui_s @Aojtek_Bw```
Где ```@masui_s @Aojtek_Bw``` теги участников в группе. Люди, которых тегнет бот при поступлении сообщения.
Тегов можно указывать до 10 через пробел. Также можно выполнить скрипт не указывая теги. В таком случае никто из участников группы не будет тегнут.

#### Заметки:
1. При монтировании удаленного ресурса желательно в опциях монтирования использовать параметр ```soft```. В случае отвала шары эта опция вернет ошибку в вызывающую команду и скрипт сможет послать алерт в лог файл и(или) телеграмм о недоступности удаленного ресурса.
2. Скприт кидает алерт в лог файл и (или) телеграмм, если на удаленном ресурсе осталось меньше 20% свободного места.
3. Скприт кинет error в лог файл и (или) телеграмм, если на удаленном ресурсе не осталось свободного места или удаленный ресур недоступен для записи.
4. Копируются файлы только в формате mp3 и не старше последних 5 минут 