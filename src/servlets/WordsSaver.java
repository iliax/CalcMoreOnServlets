package servlets;

import java.io.*;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import beans.*;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.gson.Gson;

public class WordsSaver  extends HttpServlet {
	
	private static final long serialVersionUID = -7149929571408018080L;

	private static final Logger log = Logger.getLogger(WordsSaver.class.getName());
	
	private static DatastoreService datastore= DatastoreServiceFactory.getDatastoreService();
		
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp) 
			throws IOException {
		
		resp.setContentType("application/json"); 
		
		String jsonResult = null;
		
		if((req.getPathInfo() != null) && (req.getPathInfo().equals("/count"))){
			jsonResult = datastore.prepare(new Query("WordCountPair"))
	                .asList(FetchOptions.Builder.withDefaults()).size() + "";
		}
		else {	
			try {
				jsonResult = new Gson().toJson( datastore.prepare(new Query("WordCountPair"))
		                .asList(FetchOptions.Builder.withDefaults()));
			} catch(RuntimeException exception){
				sendError(resp, 500, "error while retriving words list");
	            return;
			}
		}
		
		resp.getWriter().println(jsonResult);

	}
	
	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {	
		
		resp.setContentType("application/json");
		
		try {
			clearWordsDB();
		} catch(Exception e){
			resp.getWriter().print(new Gson().toJson(new ErrorResult("not deleted!", 500)));
		}

		resp.getWriter().print(new Gson().toJson(new SuccessResult("deleted!", 200)));
		
		log.severe("clearing db");
	}
	
	
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) 
			throws ServletException, IOException {
		
		resp.setContentType("application/json");
		
		StringBuffer jsonReqBuffer = new StringBuffer(); 
		String temp ;
		
		BufferedReader bufferedReader=new BufferedReader(req.getReader());
		
		while( (temp = bufferedReader.readLine()) != null ){
			jsonReqBuffer.append(temp);
		}
		
		JsonWordCountPair countPair;
		try {
			countPair=new Gson().fromJson(jsonReqBuffer.toString(), JsonWordCountPair.class);
		} catch(Exception e){
			sendError(resp, 500);
            return;
		}
		
		try {
			saveOrUpdateWord(countPair.getWord(), countPair.getCount());
		} catch(RuntimeException e){
			log.warning("err while saving word");
			sendError(resp, 500, "error while saving word");
			throw e;
		}
		
		resp.getWriter().print(new Gson().toJson(new SuccessResult()));
	}
	
	
	
	private void sendError(HttpServletResponse resp , int code , String mess) throws IOException{
		resp.setStatus(code);
		resp.getWriter().print(new Gson().toJson(new ErrorResult(mess,code)));
	}
	
	private void sendError(HttpServletResponse resp, int code) throws IOException{
		resp.setStatus(code);
		resp.getWriter().print(new Gson().toJson(new ErrorResult(code)));
	}
	
	
	
    @SuppressWarnings("unused")
	private void printDB(){
    	
        List<Entity> asList = 
                datastore.prepare(new Query("WordCountPair"))
                    .asList(FetchOptions.Builder.withDefaults());
        System.out.println("\n\n   WORDS:\n"+asList);
       
    }
    
    public void clearWordsDB(){
    	try {
	    	List<Entity> asList = 
	                datastore.prepare(new Query("WordCountPair"))
	                    .asList(FetchOptions.Builder.withDefaults());
	
	    	for(Entity entity : asList){
	    		datastore.delete(entity.getKey());
	    	}
	    	
    	} catch(RuntimeException  e){
    		log.severe("problem while clearing db");
    		throw e;
    	}
    }
    
	
    public void saveOrUpdateWord(String word, Long count){
       
		    Key entityKey=KeyFactory.createKey("WordCountPair", word);
		
		    Transaction tx; 
		    
		    for(;;){
		        
		        try {
		            
		            tx = datastore.beginTransaction();
		        
		            try {
		                
		                Entity existed = datastore.get(tx,entityKey);
		                Long currCount = (Long)existed.getProperty("count");
		                existed.setProperty("count", currCount+count);
		                datastore.put(tx,existed);
		
		                //System.out.println("UPDATED!");
		                
		            } catch(EntityNotFoundException enfe){
		                
		                Entity newEntity = new Entity(entityKey);
		                newEntity.setProperty("count",count);
		                datastore.put(tx, newEntity);
		
		                //System.out.println("SAVED!");
		            }
		            
		        }
		        /*
		         * в доке сказано что put может кидать ConcurrentModificationException,
		         * на практике ни разу не кинул. кидает tx при коммите
		         */
		        catch(java.util.ConcurrentModificationException  e){
		        	log.severe("ConcurrentModificationException in datastore.put method");
		            continue;
		        }
		        
		        
		        try {
		            tx.commit();
		        }
		        /*
		         * если транзакция уже выполнена, откачена, 
		         * либо попытка завершить или откатить ее уже завершилась неудачей.
		         */
		        catch(java.lang.IllegalStateException e){
		        	log.severe("IllegalStateException");
		            continue;
		        }
		        /*
		         * при возникновении ошибки в хранилище данных.
		         */
		        catch(DatastoreFailureException  e){
		        	log.severe("DatastoreFailureException");
		        	throw e;
		            //continue;
		        }
		        /*
		         * вот здесь то все и происходит при конкуррентной записи
		         */
		        catch(java.util.ConcurrentModificationException e){
		            continue;
		        }
		        // стремное место, хз что ловить и как обрабатывать
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
    }
  
}


















