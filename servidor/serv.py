import os
from flask import Flask, request, redirect, url_for
from werkzeug import secure_filename
#Ruta hacia el directorio uploads de donde tengas el servidor 
UPLOAD_FOLDER = '/home/bryan/Escritorio/servidor/uploads/'
#Ruta para modificar el archivo task y reconocer la imagen
TASK_TXT=UPLOAD_FOLDER+'tasks.txt'
#De momento solo admite  imagenes .jpg
ALLOWED_EXTENSIONS = set(['jpg'])
 
app = Flask(__name__)
#Añadimos el directorio donde se suben los archivos a la configuracion del servidor
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
#Se comprueba que el archivo es de la extenssion  o extensisones requeridas
def allowed_file(filename):
    return '.' in filename and \
           filename.rsplit('.', 1)[1] in ALLOWED_EXTENSIONS
#Url principal asociada al metodo indeex
@app.route("/", methods=['GET', 'POST'])
def index():
    if request.method == 'POST':
        file = request.files['file']
        if file and allowed_file(file.filename):
            filename = secure_filename(file.filename)
            file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
	    f=open(TASK_TXT,'w')#Escribimos el nombre de la aimagen en el fichero
	    f.write(file.filename+'\n')
	    f.close()
            return redirect(url_for('index'))#Hacemos que el cliente vuelva a la url principal
	#Html que visualizara el cliente
    return """
    <!doctype html>
    <title>Upload new File</title>
    <h1>Upload new File</h1>
    <form action="" method=post enctype=multipart/form-data>
      <p><input type=file name=file>
         <input type=submit value=Upload>
    </form>
    <p>%s</p>
    """ % "<br>".join(os.listdir(app.config['UPLOAD_FOLDER'],))
 
if __name__ == "__main__":
    app.run(host='0.0.0.0', port=int(80), debug=True)
