import cgi
from http.server import HTTPServer, BaseHTTPRequestHandler
import socketserver

import json


class Server(BaseHTTPRequestHandler):
    def _set_headers(self):#устанавливаем заголовки http ответа
        self.send_response(200)#код состояния ответа
        self.send_header('Content-type', 'application/json')#уставновление заголовка ответа в формате джсон
        self.end_headers()# пустая строка в качестве окончания заголовков ответа

    def do_HEAD(self):
        self._set_headers()

    # GET sends back a Hello world message
    def do_GET(self):#обработка гет запросов
        #загрузка даннных из файла
        data = json.load(open('data.json', 'r', encoding='utf-8-sig'))

        self._set_headers()
        #преобразование данных в формате дж в строку, отправляющуюся потом в ответ на гет запрос
        self.wfile.write(json.dumps(data).encode())

    # POST echoes the message adding a JSON field
    def do_POST(self):# обработка пост запросов
        #разбор заголовков и возврат типа и словаря со значениями параметтров
        ctype, pdict = cgi.parse_header(self.headers.get('content-type'))


        # refuse to receive non-json content
        if ctype != 'application/json':# проверка типа контента
            self.send_response(400)
            self.end_headers()
            return

        # read the message and convert it into a python dictionary
        # получение длины данных пост запроса
        length = int(self.headers.get('content-length'))
        #сохранение данных из пост запроса
        message = json.loads(self.rfile.read(length))
        # сохранение данных из переменной в файл
        json.dump(message, open('data.json', 'w', encoding='utf-8'))
        # add a property to the object, just to mess with data
        message['received'] = 'ok'

        # send the message back
        self._set_headers()
        #преобр данные в строку, окторая отправляется в ответ на пост запрос
        self.wfile.write(json.dumps(message).encode())

#создание экземпляра сервера и его запуск
def run(server_class=HTTPServer, handler_class=Server, port=8080):
    server_address = ('', port)# кортеж из пустой строки и номера порта
    httpd = server_class(server_address, handler_class)# создание экземпляра сервера

    print('Starting httpd on port %d...' % port)
    httpd.serve_forever()# бескон сервер,прослушивание вход соединений


if __name__ == "__main__":
    from sys import argv

    #проверка, что в командной строке указан только порт
    if len(argv) == 2:
        run(port=int(argv[1]))#запуск сервера по данному порту
    else:
        run()#запуск на порту 8080
