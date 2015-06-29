#Imports para la traduccion
import sys
from BeautifulSoup import BeautifulSoup
import urllib2
import urllib
#---------------------
import os
#import matlab_wrapper
#import goslate #Libreria de traduccion
from flask import render_template
from flask import Flask, request, redirect, url_for, Response
from werkzeug import secure_filename
#Ruta hacia el directorio de NeuralTalk
NEURAL_FOLDER='/home/bryan/neuraltalk/'
#Ruta hacia el directorio del servidor
SERV_FOLDER = '/home/bryan/Proyecto-Fin-de-Grado/servidor/'
#Ruta hacia el directorio uploads de donde tengas el servidor 
UPLOAD_FOLDER = SERV_FOLDER+'uploads/'
#Rute hacia los templates
TEMPLATE_FOLDER=SERV_FOLDER+'templates/'
#Ruta para modificar el archivo task y reconocer la imagen
TASK_TXT=UPLOAD_FOLDER+'tasks.txt'
#De momento solo admite  imagenes .jpg
ALLOWED_EXTENSIONS = set(['jpg','JPG','jpeg','JPEG'])
 
app = Flask(__name__)
#Annadimos el directorio donde se suben los archivos a la configuracion del servidor
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
	    f=open(TASK_TXT,'w')#Escribimos el nombre de la imagen en el fichero
	    f.write(file.filename+'\n')
	    f.close()
	    #Ejecutamos el script para extraer las caractersticas de la imagen
	    os.system("sudo -u bryan "+SERV_FOLDER+"/matlab.sh")
	    #Ejecutamos el NeuralTalk para predecir el caption
	    os.system("python "+NEURAL_FOLDER+"predict_on_images.py "+NEURAL_FOLDER+"cv/model_checkpoint_coco_visionlab43.stanford.edu_lstm_11.14.p -r "+UPLOAD_FOLDER)
	    os.system("rm "+UPLOAD_FOLDER+file.filename)
	    #Movemos el result.html a la carpeta templates
	    #os.system("cp "+UPLOAD_FOLDER+"result.html "+TEMPLATE_FOLDER)
	    #Abrimos el fichero y extraemos solamente el caption, ose laa prediccion
	    html_file=open(UPLOAD_FOLDER+"result.html","r")
	    strs=html_file.read()
       	    strs=strs.split(')')
	    strs=strs[1]
	    strs=strs.split('<')
	    strs=strs[0] 
	    strs=traducir(strs)
	    #Escribiendo en el html el resultado final
	    html_file.close()
	    #html_file=open(TEMPLATE_FOLDER+"result.html","w")
	    #html_file.write("<html><body>"+strs+"<br></body></html>")
	    #html_file.close()
	    print request.headers
        return strs
    #Html que visualizara el cliente cuando se hace peticion get desde navegador
    return render_template("index.html")

def traducir(string='Hola'):
	data = {'sl':'en','tl':'es','text':string} 
	querystring = urllib.urlencode(data)
	request = urllib2.Request('http://www.translate.google.com' + '?' + querystring )
	request.add_header('User-Agent', 'Mozilla/5.0 (Windows; U; Windows NT 5.1; it; rv:1.8.1.11) Gecko/20071127 Firefox/2.0.0.11')
	opener = urllib2.build_opener()
	feeddata = opener.open(request).read()
	soup = BeautifulSoup(feeddata)
	strs=soup.find('span', id="result_box")
	strs=str(strs)
	strs=strs.split('">')
	strs=strs[3]
	strs=strs.split('<')
	strs=strs[0]
	return strs

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=int(80), debug=True,threaded=True)
