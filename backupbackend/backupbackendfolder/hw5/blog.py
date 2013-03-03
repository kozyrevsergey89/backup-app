
import os
import cgi
import re
import random
import hashlib
import hmac
import logging
import json
from string import letters
from gcm import GCM
from urlparse import parse_qs
import webapp2
import jinja2
from datetime import datetime, timedelta
from google.appengine.ext import db
import webapp2_extras
from google.appengine.ext import webapp
from google.appengine.ext import blobstore
from google.appengine.ext import db
from google.appengine.ext.webapp import blobstore_handlers
from google.appengine.ext.webapp.util import run_wsgi_app
import logging
from threading import Timer
import time

template_dir = os.path.join(os.path.dirname(__file__), 'templates')
jinja_env = jinja2.Environment(loader = jinja2.FileSystemLoader(template_dir),
                               autoescape = True)

secret = 'fart'

def render_str(template, **params):
    t = jinja_env.get_template(template)
    return t.render(params)

def make_secure_val(val):
    return '%s|%s' % (val, hmac.new(secret, val).hexdigest())

def check_secure_val(secure_val):
    val = secure_val.split('|')[0]
    if secure_val == make_secure_val(val):
        return val

class BlogHandler(webapp2.RequestHandler):
    def write(self, *a, **kw):
        self.response.out.write(*a, **kw)

    def render_str(self, template, **params):
        params['user'] = self.user
        t = jinja_env.get_template(template)
        return t.render(params)

    def render(self, template, **kw):
        self.write(self.render_str(template, **kw))
    
    def get_user(self):
        uname = self.request.cookies.get('user_id')
        uc = []
        logging.info(self.request)
        uc = uname.split('|')
        u=''
        try:
            u = User.by_id(int(uc[0]))
        except:
            print "not even a user's request"
        return u

    def render_json(self, d):
        json_txt = json.dumps(d)
        self.response.headers['Content-Type'] = 'application/json; charset=UTF-8'
        self.write(json_txt)

    def set_secure_cookie(self, name, val):
        cookie_val = make_secure_val(val)
        self.response.headers.add_header(
            'Set-Cookie',
            '%s=%s; Path=/' % (name, cookie_val))

    def read_secure_cookie(self, name):
        cookie_val = self.request.cookies.get(name)
        return cookie_val and check_secure_val(cookie_val)

    def login(self, user):
        self.set_secure_cookie('user_id', str(user.key().id()))

    def logout(self):
        self.response.headers.add_header('Set-Cookie', 'user_id=; Path=/')

    def initialize(self, *a, **kw):
        webapp2.RequestHandler.initialize(self, *a, **kw)
        uid = self.read_secure_cookie('user_id')
        self.user = uid and User.by_id(int(uid))

        if self.request.url.endswith('.json'):
            self.format = 'json'
        else:
            self.format = 'html'

class MainPage(BlogHandler):
  def get(self):
      self.write('Hello, Udacity!')


##### user stuff
def make_salt(length = 5):
    return ''.join(random.choice(letters) for x in xrange(length))

def make_pw_hash(name, pw, salt = None):
    if not salt:
        salt = make_salt()
    h = hashlib.sha256(name + pw + salt).hexdigest()
    return '%s,%s' % (salt, h)

def valid_pw(name, password, h):
    salt = h.split(',')[0]
    return h == make_pw_hash(name, password, salt)

def users_key(group = 'default'):
    return db.Key.from_path('users', group)

class User(db.Model):
    name = db.StringProperty(required = True)
    pw_hash = db.StringProperty(required = True)
    email = db.StringProperty()
    #fun zone starts right here!!!
    #gps
    is_gps_current = db.StringProperty()
    gps_timestamp = db.DateTimeProperty()
    latitude = db.StringProperty()
    longitude = db.StringProperty()
    #phone info
    ip_address = db.StringProperty()
    phone = db.StringProperty()
    acc_list = db.StringListProperty()
    device_id = db.StringProperty()
    #backup
    backup_file_key = blobstore.BlobReferenceProperty(blobstore.BlobKey, required=False)
    backup_timestamp = db.DateTimeProperty()
    user_timestamp = db.DateTimeProperty(auto_now = True)
    #google cloud messaging
    registration_id = db.StringProperty()
    is_admin = db.StringProperty()
    
    @classmethod
    def by_simple_id(cls, id):
        users = db.GqlQuery("SELECT * "
                            "FROM User "
                            "WHERE id = 16 "#, id
                           )
        r=users.run(limit=1)
        #logging.debug("value of my var is %s", str(id))
        u = r.next()
        r=None
        return u 

    @classmethod
    def by_id(cls, uid):
        return User.get_by_id(uid, parent = users_key())

    @classmethod
    def by_name(cls, name):
        u = User.all().filter('name =', name).get()
        return u
    
    @classmethod
    def by_regid(cls, regId):
        u = User.all().filter('registration_id =', regId).get()
        return u
    
    @classmethod
    def by_device_id(cls, device_id):
        u = User.all().filter('device_id =', device_id).get()
        return u
    
    
    @classmethod
    def by_idi(cls, idi):
        u = User.all().filter('id =', idi).get()
        return u
    
    @classmethod
    def register(cls, name, pw, email = None, is_admin = 'false'):
        pw_hash = make_pw_hash(name, pw)
        return User(parent = users_key(),
                    name = name,
                    pw_hash = pw_hash,
                    email = email, is_admin = is_admin)

    @classmethod
    def login(cls, name, pw):
        u = cls.by_name(name)
        if u and valid_pw(name, pw, u.pw_hash):
            return u


##### blog stuff

def blog_key(name = 'default'):
    return db.Key.from_path('blogs', name)

class Post(db.Model):
    subject = db.StringProperty(required = True)
    content = db.TextProperty(required = True)
    created = db.DateTimeProperty(auto_now_add = True)
    last_modified = db.DateTimeProperty(auto_now = True)

    def render(self):
        self._render_text = self.content.replace('\n', '<br>')
        return render_str("post.html", p = self)

    def as_dict(self):
        time_fmt = '%c'
        d = {'subject': self.subject,
             'content': self.content,
             'created': self.created.strftime(time_fmt),
             'last_modified': self.last_modified.strftime(time_fmt)}
        return d



class BlogFront(BlogHandler):
    def get(self):
        posts = greetings = Post.all().order('-created')
        if self.format == 'html':
            self.render('front.html', posts = posts)
        else:
            return self.render_json([p.as_dict() for p in posts])

class PostPage(BlogHandler):
    def get(self, post_id):
        key = db.Key.from_path('Post', int(post_id), parent=blog_key())
        post = db.get(key)

        if not post:
            self.error(404)
            return
        if self.format == 'html':
            self.render("permalink.html", post = post)
        else:
            self.render_json(post.as_dict())



class NewPost(BlogHandler):
    def get(self):
        if self.user:
            self.render("newpost.html")
        else:
            self.redirect("/login")

    def post(self):
        if not self.user:
            self.redirect('/blog')

        subject = self.request.get('subject')
        content = self.request.get('content')

        if subject and content:
            p = Post(parent = blog_key(), subject = subject, content = content)
            p.put()
            self.redirect('/blog/%s' % str(p.key().id()))
        else:
            error = "subject and content, please!"
            self.render("newpost.html", subject=subject, content=content, error=error)


USER_RE = re.compile(r"^[a-zA-Z0-9_-]{3,20}$")
def valid_username(username):
    return username and USER_RE.match(username)

PASS_RE = re.compile(r"^.{3,20}$")
def valid_password(password):
    return password and PASS_RE.match(password)

EMAIL_RE  = re.compile(r'^[\S]+@[\S]+\.[\S]+$')
def valid_email(email):
    return not email or EMAIL_RE.match(email)

class TermsOfUse(BlogHandler):
    def get(self):
        self.render("terms_of_use.html")

    
class PrivacyPolicy(BlogHandler):
    def get(self):
        self.render("privacy_policy.html")

class Signup(BlogHandler):
    def get(self):
        u = self.get_user()
        if u and u.name == 'administrator':
            self.render("signup-form.html")
        else:
            self.redirect('/')

        

    def post(self):
        u = self.get_user()
        if u and u.name == 'administrator':
            have_error = False
            self.username = self.request.get('username')
            self.password = self.request.get('password')
            self.verify = self.request.get('verify')
            self.email = self.request.get('email')
    
            params = dict(username = self.username,
                          email = self.email)
    
            if not valid_username(self.username):
                params['error_username'] = "That's not a valid username."
                have_error = True
    
            if not valid_password(self.password):
                params['error_password'] = "That wasn't a valid password."
                have_error = True
            elif self.password != self.verify:
                params['error_verify'] = "Your passwords didn't match."
                have_error = True
    
            if not valid_email(self.email):
                params['error_email'] = "That's not a valid email."
                have_error = True
    
            if have_error:
                self.render('signup-form.html', **params)
            else:
                self.done()
        else:
            self.response.headers['Content-Type'] = "text/plain"
            self.response.out.write('nice try))')

    def done(self, *a, **kw):
        raise NotImplementedError

class Unit2Signup(Signup):
    def done(self):
        self.redirect('/unit2/welcome?username=' + self.username)

class Register(Signup):
    def done(self):
        #make sure the user doesn't already exist
        u = User.by_name(self.username)
        if u:
            msg = 'That user already exists.'
            self.render('signup-form.html', error_username = msg)
        else:
            u = User.register(self.username, self.password, self.email)
            u.put()
            self.login(u)
            self.redirect('/')

#updating data from registered user

#gps
#latitude = db.StringProperty()
#longitude = db.StringProperty()
#phone info
#ip_address = db.StringProperty()
#phone = db.StringProperty()
#acc_list = db.StringListProperty()
#backup
#backup_timestamp = db.StringProperty(auto_now = True)
#google cloud messaging
#registration_id = db.StringProperty()
class UpdateUser(BlogHandler):
    def post(self):
        logging.info(self.request)
        if self.user:
            u = self.get_user()
            
            #getting data
            #print self.request    
            if self.request.get('ip') or self.request.get('phone') or self.request.get('acc'):
                if self.request.get('ip'):
                    ip = self.request.get('ip')
                    u.ip_address = str(ip)
                if self.request.get('phone'):
                    phone = self.request.get('phone')
                    u.phone = str(phone)
                if  self.request.get('acc'):
                    acc = parse_qs('acc='+self.request.get('acc'))
                    acc = acc['acc'][0]
                    logging.debug(acc)
                    acli = []
                    for a in acc.split(','):
                        acli.append(a)
                    print acli
                    u.acc_list = acli
                u.put()
                self.response.headers['Content-Type'] = "text/plain"
                self.response.out.write('upd info')
                    
            if self.request.get('longitude') and self.request.get('latitude'):# and self.request.get('current'):
                longitude = self.request.get('longitude')
                latitude = self.request.get('latitude')
                is_gps_current = self.request.get('current')
                u.is_gps_current = is_gps_current
                u.gps_timestamp = datetime.now()
                u.longitude = longitude
                u.latitude = latitude
                u.put()
                self.response.headers['Content-Type'] = "text/plain"
                self.response.out.write('update location')
            
class IsAdminOnDevice(BlogHandler):
    def post(self):
        if self.user:
            u = self.get_user()
            if self.request.headers['admin']:
                u.is_admin = self.request.headers['admin']
                u.put()
            self.response.headers['Content-Type'] = "text/plain"
            self.response.out.write('update admin status')
            
            
            
#uploading backup file from android
# Returns only the URL in which the file will be uploaded, so this URL may be used in client for upload the file
class GetBlobstoreUrl(webapp.RequestHandler):
    def get(self):
        #if self.user:
            logging.info('here')
            upload_url = blobstore.create_upload_url('/upload')
            logging.info("url blob %s", upload_url)
            self.response.headers['backurl'] = upload_url
            self.response.out.write('url in header')

class UploadHandler(blobstore_handlers.BlobstoreUploadHandler):
    def post(self):
        #if self.user:
            uname = self.request.cookies.get('user_id')
            uc = []
            uc = uname.split('|')
            u = User.by_id(int(uc[0]))#u = User.by_simple_id(uc[0])
            logging.info('inside upload handler')
            logging.info('!!!!!!!!!!!!!!!%s', self.request.params.items())
            logging.info('!!!!!!!!!!!!???????????!!!%s', self.get_uploads())
            upload_files = []
            upload_files = self.get_uploads('file')
            logging.info('!!!!!!!!!!!!!!!!', self.request)
            blob_info = upload_files[0]
            u.backup_file_key = blob_info.key()
            u.backup_timestamp = datetime.now()
            u.put()
            self.response.out.write('good)')
            
class ServeHandler(blobstore_handlers.BlobstoreDownloadHandler):
    def get(self):
        #if self.user:
            uname = self.request.cookies.get('user_id')
            uc = []
            uc = uname.split('|')
            u = User.by_id(int(uc[0]))
            self.send_blob(u.backup_file_key)
#!!!!!!!implement secure cookie check!!!!!!
#END of working with backup


class CheckGCM(webapp2.RequestHandler):
    def post(self):
        #if self.user:
            logging.getLogger().setLevel(logging.DEBUG)
            uname = self.request.cookies.get('user_id')
            uc = []
            logging.error(self.request)            
            uc = uname.split('|')
            logging.info(self.request)
            u1 = User.by_id(int(uc[0]))
            regId = self.request.get('regId')
            device_id = self.request.get('device_id')
            u2 = User.by_device_id(device_id)
            if u2 and u1.name != u2.name:
                self.response.headers['Content-Type'] = "text/plain"
                self.response.headers['Second-User'] = 'true'
                self.response.out.write('phone already has owner')
            else:
                logging.error('before '+device_id)
                if (device_id == u1.device_id) or (u1.device_id is None):  
                    u1.registration_id = regId
                    logging.error('first '+device_id)
                    u1.device_id = device_id 
                    u1.put()
                    self.response.headers['Content-Type'] = "text/plain"
                    self.response.headers['use_full'] = "true"
                    self.response.out.write('done registration')
                elif self.request.headers.get('reinit'):
                    if self.request.headers.get('reinit') == 'true':
                        #deregister old app before!!!!!!!!!!!!!!!!!
                        gcm = GCM('AIzaSyArovi8drs_93MM4K9n5WjlbQ9XxthHWuo')
                        try:
                            gcm.plaintext_request(registration_id=u1.registration_id, data={'action':'deregister'})
                        except:
                            logging.error('the registration id is not valid anymore')
                        u1.registration_id = regId
                        logging.error('second '+device_id)
                        u1.device_id = device_id
                        #all old device data to None
                        u1.is_gps_current = None
                        u1.gps_timestamp = None
                        u1.latitude = None
                        u1.longitude = None
                        #phone info
                        u1.ip_address = None
                        u1.phone = None
                        u1.acc_list = []
                        u1.is_admin = None
                        #write changes to database
                        u1.put()
                        self.response.headers['Content-Type'] = "text/plain"
                        self.response.headers['use_full'] = "true"
                        self.response.out.write('done registration')
                else:
                    logging.error('third '+device_id)
                    self.response.headers['Content-Type'] = "text/plain"
                    self.response.headers['use_full'] = "false"
                    self.response.out.write('done registration')
                
            
class InitGCM(webapp2.RequestHandler):
    def get(self):
        #if self.user:
            uname = self.request.cookies.get('user_id')
            uc = []
            uc = uname.split('|')
            u = User.by_id(int(uc[0]))
            gcm = GCM('AIzaSyArovi8drs_93MM4K9n5WjlbQ9XxthHWuo')
            data = {'param1': 'value', 'param2': 'value2'}
            # Plaintext request
            gcm.plaintext_request(registration_id=u.registration_id, data=data)
            self.response.out.write(self.request.body)

#answer json to browser
class JsonAnswerSimple(BlogHandler):
    def post(self):
        if self.user:
            u = self.get_user()
            checktime = u.backup_timestamp
            #prepare GCM
            gcm = GCM('AIzaSyArovi8drs_93MM4K9n5WjlbQ9XxthHWuo')
            data = {}
            
            args = json.loads(self.request.body)
            logging.debug(args)
            r = {'success':'false','val':''}
            if args:
                if args['val']:
                    r['val'] = args['val']
                    if args['action'] == 'wipe':
                        data['action'] = 'wipe'
                        if u.is_admin == 'true':
                            #do not send gcm wipe request if not aquired admin rights
                            gcm.plaintext_request(registration_id=u.registration_id, data=data)
                            r['success'] = 'true'
                    if args['action'] == 'get_gps':
                        data['action'] = 'get_gps'
                        gcm.plaintext_request(registration_id=u.registration_id, data=data)
                        r['success'] = 'true'
                    if args['action'] == 'get_info':
                        data['action'] = 'get_info'
                        gcm.plaintext_request(registration_id=u.registration_id, data=data)
                        r['success'] = 'true'
                    if args['action'] == 'get_backup':
                        #u = User.by_id(int(uc[0]))
                        if u.backup_timestamp:
                            r['timestamp'] = str(u.backup_timestamp.year) +' '+ str(u.backup_timestamp.month) +' '+  str(u.backup_timestamp.day) 
                            r['success'] = 'true'
                    if args['action'] == 'find_phone':
                        data['action'] = 'find_phone'
                        gcm.plaintext_request(registration_id=u.registration_id, data=data)
                        r['success'] = 'true'
            self.render_json(r)
            
            
class JsonAnswer(BlogHandler):
    def post(self):
        if self.user:
            #get current user
            u = self.get_user()
            #prepare GCM
#            gcm = GCM('AIzaSyArovi8drs_93MM4K9n5WjlbQ9XxthHWuo')
            #data = {}
            #gcm.plaintext_request(registration_id=u.registration_id, data=data)
            #!!!!!!!!! Plaintext request
            args = json.loads(self.request.body)
            r = {'success':'false'}
            if args:
                if args['val']:
                    r['val'] = args['val']
                if args['action'] == 'wipe':
                    
                    #data['action'] = 'wipe'
 #                   gcm.plaintext_request(registration_id=u.registration_id, data=data)
                    
                    #if feedback['success']:
                    r['admin'] = u.is_admin
                    r['success'] = 'true'
                if args['action'] == 'find_phone':
                    r['success'] = 'true'
                    
                if args['action'] == 'get_gps':
                    #data['action'] = 'get_gps'
  #                  gcm.plaintext_request(registration_id=u.registration_id, data=data)
                    #while checktime == u.backup_timestamp:
                     #   time.sleep(1)
   #                 logging.debug('get gps!!!!')
    #                u=None
                    #if feedback['success']:
     #               u = User.by_id(int(uc[0]))
                    if u.longitude and u.latitude and u.is_gps_current:
                        if ((datetime.now() - u.gps_timestamp) < timedelta (hours = 1)):
                            r['is_current'] = 'true'
                        else:
                            r['is_current'] = 'false'
                        
                        r['longitude'] = u.longitude
                        r['latitude'] = u.latitude
                        r['success'] = 'true'
                if args['action'] == 'get_info':
                    if u.phone or u.ip_address or u.acc_list:
                        if u.phone:
                            r['phone_number'] = u.phone
                        if u.ip_address:
                            r['ip_address'] = u.ip_address
                        if u.acc_list:
                            acc = []
                            for a in u.acc_list:
                                acc.append(a)
                            r['acc_list'] = acc
                        r['success'] = 'true'
                if args['action'] == 'get_backup':
                    #u = User.by_id(int(uc[0]))
                    if u.backup_timestamp:
                        r['timestamp'] =  str(u.backup_timestamp.day)+'/'+ str(u.backup_timestamp.month) +'/'+str(u.backup_timestamp.year)   
                        r['success'] = 'true'
                
            self.render_json(r)
            
        


class Login(BlogHandler):
    def get(self):
        self.render('login-form.html')

    def post(self):
        username = self.request.get('username')
        password = self.request.get('password')

        u = User.login(username, password)
        if u:
            self.login(u)
            self.redirect('/')
        else:
            msg = 'Invalid login'
            self.render('login-form.html', error = msg)

class Logout(BlogHandler):
    def get(self):
        self.logout()
        self.redirect('/login')

class Unit3Welcome(BlogHandler):
    def get(self):
        if self.user:
            self.render('welcome.html', username = self.user.name)
        else:
            self.redirect('/login')

class Welcome(BlogHandler):
    def get(self):
        username = self.request.get('username')
        if valid_username(username):
            self.render('welcome.html', username = username)
        else:
            self.redirect('/unit2/signup')

app = webapp2.WSGIApplication([#('/fff', MainPage),
                               #('/unit2/signup', Unit2Signup),
                               #('/unit2/welcome', Welcome),
                               #('/blog/?(?:.json)?', BlogFront),
                               #('/blog/([0-9]+)(?:.json)?', PostPage),
                               #('/blog/newpost', NewPost),
                               ('/signup', Register),
                               ('/checkgcm/register', CheckGCM),
                               ('/initgcm', InitGCM),
                               ('/login', Login),
                               ('/logout', Logout),
                               ('/', Unit3Welcome),
                               ('/backgeturl', GetBlobstoreUrl),
                               ('/upload', UploadHandler),
                               ('/backserve', ServeHandler),
                               ('/updateuser', UpdateUser),
                               ('/jsonanswer', JsonAnswer),
                               ('/jsonanswerfirst', JsonAnswerSimple),
                               ('/isadmin', IsAdminOnDevice),
                               ('/terms_of_use', TermsOfUse),
                               ('/privacy_policy', PrivacyPolicy),
                               ],
                              debug=True)

