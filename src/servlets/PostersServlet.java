package servlets;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.Poster;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;

public class PostersServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger log = Logger.getLogger(PostersServlet.class.getName());
	
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		super.doGet(req, resp);
	}
	
	
	//not working yet
	private Poster.STATUS saveNewPoster(String nick){
		
	
	    Key entityKey=KeyFactory.createKey("Posters", nick);
		
	    Transaction tx; 
	    
	    for(;;){
	        
	        try {
	            
	            tx = datastore.beginTransaction();
	        
	            Entity entity=new Entity(entityKey);
	            entity.setProperty("status", Poster.STATUS.IN_PROGRESS);
	            entity.setProperty("statusUpdate", System.currentTimeMillis());
	            
	            datastore.put(entity);
	            
	        }
	        catch(java.util.ConcurrentModificationException  e){
	        	log.severe("ConcurrentModificationException in datastore.put method");
	            continue;
	        }
	        
	        
	        try {
	            tx.commit();
	        }
	        catch(java.lang.IllegalStateException e){
	        	log.severe("IllegalStateException");
	            //continue;
	        }
	        catch(DatastoreFailureException  e){
	        	log.severe("DatastoreFailureException");
	        	throw e;
	            //continue;
	        }
	        catch(java.util.ConcurrentModificationException e){
	            //continue;
	        }
	        finally {
	            try {
	                if(tx.isActive()){
	                    tx.rollback();
	                    log.warning("tx rolled back!");
	                }
	            }
	             catch(IllegalStateException  e){
	                log.severe("Error while rolling back");
	                throw e; 
	            } catch(RuntimeException  e){
	            	log.severe("unknown Error while rolling back");
	            	throw e; 
	            }
	            
	        }
	
	        /*
	         * если tx нормально  закоммитился
	         */
	        break;
	    }
	
	
	    // TODO
		return null;
	}

	
	//not working yet
	private Poster.STATUS checkPosterExistance(String nick){
		return Poster.STATUS.FORGOTTEN;
	}

}
