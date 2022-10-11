Для генерации отчетов необходимо содержимое папки bigbluebutton скопировать с заменой в католог

/usr/local/bigbluebutton/

Это добавит необходимые скрипты и обновит файл стандартного скрипта events.rb. В него добавятся следующе строчки:

#Make a report
script_path = File.dirname(__FILE__) + "/create_vebinar_report.rb";
system("ruby #{script_path} -m #{meeting_id}")

При такой конфигурации отчет по видеоконференции будет создаваться вне зависимости от того, велась ли запись конференции.
Если неообходимо иное поведение: отчет формируется только для записываемых конференций, то надо скопировать из папки

/core/scripts/events
в папку
/core/scripts/post_process

папку data co всем содержимым и файл create_vebinar_report.rb, а scripts/events/events.rb необходимо оставить без изменений.
Папка create_reports копируется в обоих случаях.

Остальные настройки на сервере bigbluebutton:

1. Добавляем необходимые пакеты:

sudo gem update --system
gem install activesupport -v 5.0.0
gem install axlsx

(Если при установке gem asxlx возникла проблема с zlib, то это решается установкой пакета sudo apt-get install zlib1g-dev)

2. Необходимо создать папку reports для отчетов и дать права на запись для пользователей:

   var/bigbluebutton/published/presentation/reports/

3. Если нужна аналитика в любом случае, даже если запись вебинара не ведется, тогда надо выставить настройку в

/usr/share/bbb-web/WEB-INF/classes/bigbluebutton.properties:

   keepEvents=true

Логи для отслеживания работы скриптов лежат /var/log/bigbluebutton/bbb-rap-worker.log

Не забываем поменять в настройках data/config.yaml урл и токен для доступа к веб-сервису Moodle:

moodleUrl: http://etd.elearning.otr.ru
wstoken: 3325886d44dc23740ad3f45601a20728

