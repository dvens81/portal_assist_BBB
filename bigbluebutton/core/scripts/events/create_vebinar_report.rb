#!/usr/bin/ruby

require_relative '../../../create_report/bbbevents2/base'
#require_relative 'bbbevents2/version'
require_relative '../../../create_report/bbbevents2/attendee'
require_relative '../../../create_report/bbbevents2/events'
require_relative '../../../create_report/bbbevents2/poll'
require_relative '../../../create_report/bbbevents2/recording'
require "rubygems"
require "net/https"
require "uri"
require "json"
require 'axlsx'
require "yaml"
require "trollop"
require 'fileutils'

#require File.expand_path('../../../lib/recordandplayback', __FILE__)

exit if ARGV.length < 1

def self.seconds_to_time(i)
  [i / 3600, i / 60 % 60, i % 60].map { |t| t.floor.to_s.rjust(2, "0") }.join(':')
end

def self.to_yes_no(i)
  if i > 0 then
    'Да'
  else
    'Нет'
  end
end

# == events.xml directory /var/bigbluebutton/recording/raw/<internal-meeting-id>/
# == publish report directory /var/bigbluebutton/published/presentation/reports/<external-meeting-id>/
# == script directory /usr/local/bigbluebutton/core/scripts/post_process/  -m param
# == access link: https://elk-bbbub.otr.ru/presentation/reports/<external-meeting-id>/Отчёт_по_вебинару.xlsx

opts = Trollop::options do
  opt :meeting_id, "Meeting id to archive", :type => String
end
meeting_id = opts[:meeting_id]

path_to_save = "/var/bigbluebutton/published/presentation/reports/"
events_xml_file = "/var/bigbluebutton/recording/raw/#{meeting_id}/events.xml"

# load Moodle configuration
configuration = YAML::load_file(File.join(File.dirname(__FILE__), 'data/config.yaml'))

criteriaKey = "criteria[0][key]=id&"
criteriaValue = "criteria[0][value]="

# request moodle ws url
wsGetUserRequestUrl = configuration['moodleUrl'] + "/webservice/rest/server.php?wstoken=" + configuration['wstoken'] + "&wsfunction=core_user_get_users&moodlewsrestformat=json&" + criteriaKey

logo_img = File.expand_path('data/eb_va_logo.png', File.dirname(__FILE__))

# Parse the recording's events.xml.
recording = BBBEvents2.parse(events_xml_file)

external_id = recording.external_id;
path_to_save = path_to_save + external_id + "/";

puts 'Start processing meeting ' + meeting_id

# Moderators string
moderators = ''
recording.moderators.each do |moder|
  moderators = moderators + moder.name + ', '
end
moderators = moderators.chomp(", ")

# create new xslx document
doc = Axlsx::Package.new

# Required for use with numbers
doc.use_shared_strings = true

col_widths = [28, 28, 20, 15, 20, 15, 15, 15, 15, 15]

doc.workbook do |wb|
  # define your regular styles
  styles = wb.styles
  title = styles.add_style :sz => 15, :b => true, :u => true, :alignment => {:horizontal => :center, :wrap_text => true}
  default = styles.add_style :sz => 10, :font_name => 'sans-serif', :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :left, :vertical => :top, :wrap_text => true}
  header = styles.add_style :sz => 10, :font_name => 'sans-serif', :b => true, :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :left, :vertical => :top, :wrap_text => true}
  col_header = styles.add_style :sz => 10, :font_name => 'sans-serif', :b => true, :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :center, :vertical => :center, :wrap_text => true}
  col_numbers = styles.add_style :sz => 10, :font_name => 'sans-serif', :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :center, :wrap_text => true}
  date_cell = styles.add_style :sz => 10, :font_name => 'sans-serif', :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :left, :vertical => :top, :wrap_text => true}, format_code: "dd mmmm yyyy"
  time_cell = styles.add_style :sz => 10, :font_name => 'sans-serif', :border => Axlsx::STYLE_THIN_BORDER, :alignment => {:horizontal => :left, :vertical => :top, :wrap_text => true}, format_code: "HH:mm:ss"
  header2 = styles.add_style :sz => 12, :font_name => 'sans-serif', :b => true, :alignment => {:horizontal => :center, :wrap_text => true}

  #workbook
  wb.add_worksheet(:name => 'Отчет о проведенном вебинаре') do |ws|

    ws.sheet_view.show_grid_lines = false

    ws.add_image(:image_src => logo_img, :end_at => true) do |image|
      image.start_at 0, 0
      image.end_at 2, 8
    end
    # gap
    ws.add_row
    ws.add_row
    ws.add_row
    ws.add_row
    ws.add_row
    ws.add_row
    ws.add_row
    ws.add_row

    ws.add_row ['Отчет о проведенном вебинаре'], :style => title
    ws.merge_cells 'A9:E9'
    # gap
    ws.add_row
    ws.add_row ['Тема', recording.metadata["meetingName"]], :style => header
    ws.add_row ['Дата вебинара', recording.start], :style => header
    ws.rows[11].cells[1].style = date_cell
    ws.add_row ['Время начала вебинара', recording.start], :style => header
    ws.rows[12].cells[1].style = time_cell
    ws.add_row ['Время окончания вебинара', recording.finish], :style => header
    ws.rows[13].cells[1].style = time_cell
    ws.add_row ['Длительность вебинара', seconds_to_time(recording.duration)], :style => header
    ws.rows[14].cells[1].style = default
    ws.add_row ['Ведущие', moderators], :style => header
    ws.rows[15].cells[1].style = default
    ws.add_row ['Общее количество участников', recording.attendees.count], :style => header
    ws.rows[16].cells[1].style = default

    # gap
    ws.add_row

    ws.merge_cells 'A19:E19'
    ws.add_row ['Участники проведенного вебинара'], :style => header2
    # gap
    ws.add_row
    ws.add_row ['Фамилия Имя Отчество', 'Организация', 'Регион', 'Телефон', 'Email', 'Время присоединения ВА', 'Время выхода из ВА', 'Длительность участия', 'Участие в опросе, голосование', 'Участие в чате'], :style => col_header
    ws.add_row %w(1 2 3 4 5 6 7 8 9 10), :style => col_numbers

    recording.attendees.each_with_index { |att, index|
      wsUserRequest = wsGetUserRequestUrl + criteriaValue + att.ext_user_id
      puts 'Requesting: ' + wsUserRequest

      uri = URI.parse(wsUserRequest)
      http = Net::HTTP.new(uri.host, uri.port)
      request = Net::HTTP::Get.new(uri.request_uri)

      res = http.request(request)
      response = JSON.parse(res.body)

      if not response["users"].nil? then
        response["users"].each { |user|
          ws.add_row [user["fullname"], user["department"], user["city"], user["phone1"], user["email"], att.joined, att.left, seconds_to_time(att.duration), to_yes_no(att.engagement[:poll_votes]), to_yes_no(att.engagement[:chats])], :style => default
          ws.rows[22 + index].cells[5].style = time_cell
          ws.rows[22 + index].cells[6].style = time_cell
        }
      end
    }
    ws.column_widths *col_widths
  end
end

# create output path
FileUtils.mkdir_p path_to_save
doc.serialize path_to_save  + 'Отчёт_по_вебинару.xlsx'

puts 'Successfully created report ' +  path_to_save + 'Отчёт_по_вебинару.xlsx'
exit 0