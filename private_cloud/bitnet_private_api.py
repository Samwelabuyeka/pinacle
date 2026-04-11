import http.server
import json
import logging
import socketserver

# Set up logging
logging.basicConfig(level=logging.INFO)

class MyHttpRequestHandler(http.server.SimpleHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/health':
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'Health check passed')
        else:
            self.send_response(404)
            self.end_headers()
            self.wfile.write(b'Not found')

    def do_POST(self):
        try:
            content_length = int(self.headers['Content-Length'])
            data = json.loads(self.rfile.read(content_length) or b'{}')  # Fixing the syntax error
            # Process the data
            logging.info('Data received: %s', data)
            self.send_response(200)
            self.end_headers()
            self.wfile.write(b'Data processed')
        except json.JSONDecodeError as e:
            logging.error('JSON decode error: %s', e)
            self.send_response(400)
            self.end_headers()
            self.wfile.write(b'Invalid JSON')
        except Exception as e:
            logging.error('Server error: %s', e)
            self.send_response(500)
            self.end_headers()
            self.wfile.write(b'Internal Server Error')

def run(server_class=http.server.HTTPServer, handler_class=MyHttpRequestHandler, port=8080):
    server_address = ('', port)
    httpd = server_class(server_address, handler_class)
    logging.info('Starting server on port %d...', port)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        pass
    httpd.server_close()
    logging.info('Server stopped.')

if __name__ == '__main__':
    run()