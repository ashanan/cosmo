login_with_root_lab_json = """{"method": "type", "params": {"id" : "loginDialogUsernameInput", "text": "root"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "cosmo"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"method":"reWriteAlert", "params":{}}
{"method": "waits.forElement", "params": {"id": "contentWrapper", "timeout": 40000}}"""

login_with_root_snarf_json = """{"method": "click", "params": {"link" : "Log in to Chandler Server"}}
{"method": "waits.forElement", "params": {"id": "loginDialogFormContainer", "timeout": 40000}}
{"params": {"milliseconds": 5000}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogUsernameInput", "text": "root"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "type", "params": {"id" : "loginDialogPasswordInput", "text": "cosmo"}}
{"params": {"milliseconds": 500}, "method": "waits.sleep"}
{"method": "click", "params": {"id" : "loginSubmitButton"}}
{"method":"reWriteAlert", "params":{}}
{"method": "waits.forElement", "params": {"id": "contentWrapper", "timeout": 40000}}"""

logout = """{"method": "click", "params": {"link" : "Log out"}}
{"params": {"url": "\/"}, "method": "open"}
{"method": "waits.forElement", "params": {"link" : "Log in to Chandler Server"}}
{"params": {"milliseconds": 1000}, "method": "waits.sleep"}
"""

from windmill.authoring import RunJsonFile
from windmill.bin import shell_objects
import windmill

lab_urls = ['http://lab.osaf.us', 'http://next.osaf.us']

def setup_module(module):
    shell_objects.load_extensions_dir(os.path.join(os.path.dirname(__file__), 'extensions'))
    if windmill.settings['TEST_URL'] in lab_urls:
        json = login_with_root_lab_json
    else:
        json = login_with_root_snarf_json
    RunJsonFile('login_with_user_json.json', lines=json.splitlines())()
    
def teardown_module(module):
    RunJsonFile('log_out.json', lines=logout.splitlines())()

    
    

