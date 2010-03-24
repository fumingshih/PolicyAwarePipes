/*
 * Copyright (c) 2008-2009,
 *
 * Digital Enterprise Research Institute, National University of Ireland,
 * Galway, Ireland
 * http://www.deri.org/
 * http://pipes.deri.org/
 *
 * Semantic Web Pipes is distributed under New BSD License.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution and
 *    reference to the source code.
 *  * The name of Digital Enterprise Research Institute,
 *    National University of Ireland, Galway, Ireland;
 *    may not be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.deri.pipes.rdf;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.deri.pipes.utils.DateToXsdDatetimeFormatter;
import org.deri.pipes.utils.RDFlogger;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFParseException;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author fuming
 * This class will try to integrate the policy from two sources with some functions
 * 1. output new policy
 * 2. generates provenance data aoubt policy mashup
 * 3. feedback to designer about conflicts to provide awareness
 */
public class PolicyMashup {

	private transient Logger logger = LoggerFactory.getLogger(PolicyMashup.class);
	private RDFlogger rdflogger;
	private URL myPolicyRDF;
	private boolean validateSource;
	private boolean validateAcross;
	private boolean validateMe;

	public static final String COMPLIANT_WITH = "http://foolme.csail.mit.edu/policy/AIR/air#compliant-with>";
	public static final String NON_COMPLIANT_WITH = "http://foolme.csail.mit.edu/policy/AIR/air#non-compliant-with>";

	private ArrayList<Map<String, String>> wsprofiles = null;

	Map<Resource,ArrayList<Statement>> policyMap = new HashMap<Resource,ArrayList<Statement>>(); //this map will store the uri of policy as key
	public PolicyMashup(URL myRDFURL){
		myPolicyRDF = myRDFURL;
		wsprofiles = new ArrayList<Map<String,String>>();

		// A list of map that has source uri as key, and many other profile settings

	}


	public void setRDFlogger(Repository rep)throws RepositoryException{
		// this set the rdf logging message to some repository, which means could be different then current context context, could be at remote rdf repos.
		rdflogger = new RDFlogger(rep);

		RepositoryConnection conn = null ;


		try{
			conn = rep.getConnection();
		}catch(RepositoryException e){

			e.printStackTrace();
		}finally{
			conn.close();
 		}

	}

	private Map<String, String>getPolicy(String policyURI)throws RepositoryException,MalformedURLException,IOException,RDFParseException{
		/*
		 * The RepositoryConnection is the those already merged the sources
		 * The Resource resource in the
		 */

		//get all the policy in morePermission, we now use CC domain to specify policy

		Map<String, String> policy = null;
		String baseURI = "http://foolme.csail.mit.edu/";
		Repository rep = new SailRepository(new MemoryStore()); // create a new memory repository for fetching the policy
		rep.initialize();
		RepositoryConnection conn = rep.getConnection();

		URL policyURL  = new URL(policyURI);
		conn.add(policyURL, baseURI,RDFFormat.N3);

   	 	String query = "SELECT policy, creator, rejectSubject,rejectPerson "
   	 		+ "FROM {policy} rdf:type {p:wspolicy}; "
   	 		+ "			p:creator {creator}; "
   	 		+ "			p:rejectSubject {rejectSubject}; "
   	 		+ "			p:rejectPerson {rejectPerson} "
   	 		+ "USING NAMESPACE "
   	 		+ " p = <http://foolme.csail.mit.edu/ns/policy#>, "
   	 		+ " rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

   	 	try{
	   	 	TupleQueryResult results = conn.prepareTupleQuery(QueryLanguage.SERQL, query.toString()).evaluate();

	   	 		try{
	   	 			while(results.hasNext()){
	   	 			BindingSet bindings = results.next();
   	 				String policy_src= bindings.getValue("policy").toString();
   	 				String creator = bindings.getValue("creator").stringValue();
   	 				String rejectSubject = bindings.getValue("rejectSubject").stringValue();
   	 				String rejectPerson = bindings.getValue("rejectPerson").stringValue();

   	 				policy = new HashMap<String, String>();

   	 				policy.put("policysrc", policy_src);
   	 				policy.put("creator", creator);
   	 				policy.put("rejectSubject", rejectSubject);
   	 				policy.put("rejectPerson",rejectPerson);


	   	 			}
	   	 		}catch(Exception e){
	   	 			System.out.print("bad!");
	   	 			e.printStackTrace();
	   	 		}
	   	 		finally{
		 			results.close();
	   	 	}
   	 	}catch(Exception e){
	   	 		 e.printStackTrace();}
   	 	finally{
			conn.close();

	   	 }



		return policy;

	}
	public String debugPolicyLogging(Repository rep)throws RepositoryException,MalformedQueryException,QueryEvaluationException{

		RepositoryConnection conn = rep.getConnection();

   	 	String query_log = "SELECT log, type,timestamp,message,srcclass,srcfunction "
	   		+ "FROM {log} rdf:type {pal:log};"
	   		+ "        pal:type {type}; "
	   		+ "		   pal:timestamp {timestamp}; "
	   		+ "        pal:message {message}; "
	   		+ "        pal:srcclass {srcclass};"
	   		+ "        pal:srcfunction {srcfunction} "
	   		+ "USING NAMESPACE "
	   		+ "  rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, "
	   		+ "  pal = <http://foolme.csail.mit.edu/ns/policyawarelog#> ";

   	 try{
   		 TupleQueryResult results = conn.prepareTupleQuery(QueryLanguage.SERQL, query_log).evaluate();
			try{
				while(results.hasNext()){

   	 				BindingSet testBindings = results.next();
   	 				String log = testBindings.getValue("log").toString();
   	 				String type = testBindings.getValue("type").toString();
   	 				String timestamp = testBindings.getValue("timestamp").toString();
   	 				String message = testBindings.getValue("message").toString();
   	 				String srcclass = testBindings.getValue("srcclass").toString();
   	 				String srcfunction = testBindings.getValue("srcfunction").toString();

   	 				System.out.println("========== Logging info  ============");
   	 				System.out.printf("log:%s\n",log);
   	 				System.out.printf("type:%s\n", type);
   	 			    System.out.printf("message:%s\n", message);
   	 				System.out.printf("timestamp:%s\n", timestamp);
   	 				System.out.printf("srcclass:%s\n\n",srcclass);
   	 				System.out.printf("srcfunction:%s\n\n",srcfunction);
				}


			}catch(Exception e){
   	 			System.out.print("bad!");
   	 			e.printStackTrace();
   	 		}finally{
   	 			results.close();
   	 		}




   	 }catch(Exception e){
	 		 e.printStackTrace();
   	 }finally{
		conn.close();
   	 	}
		return "";


	}
	private Map<String, String> getProfile(String profileURI)throws RepositoryException,MalformedURLException,IOException,RDFParseException{

		Map<String, String> wsAttr = null;
		String baseURI = "http://foolme.csail.mit.edu/";
		Repository rep = new SailRepository(new MemoryStore()); // create a new memory repository for fetching the profile
		rep.initialize();
		RepositoryConnection conn = rep.getConnection();

		ValueFactory f = rep.getValueFactory();

   	 	// get those triples that subject is a type of ws:profile, and their subject
		URL  profile  = new URL(profileURI);
		conn.add(profile, baseURI,RDFFormat.N3);

   	 	String query = "SELECT profile, subject,author,trust "
	   		+ "FROM {profile} rdf:type {ws:profile}; "
	   		+ "        ws:subject {subject}; "
	   		+ "        ws:author {author} "
	   		+ "USING NAMESPACE "
	   		+ "  ws = <http://foolme.csail.mit.edu/ns/ws#>, "
	   		+ "  rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>";

   	 	try{
	   	 	TupleQueryResult results = conn.prepareTupleQuery(QueryLanguage.SERQL, query.toString()).evaluate();

	   	 		try{
	   	 			while(results.hasNext()){
	   	 			BindingSet bindings = results.next();
   	 				String profile_src= bindings.getValue("profile").toString();
   	 				String subject = bindings.getValue("subject").stringValue();
   	 				String author = bindings.getValue("author").toString();

   	 				wsAttr = new HashMap<String, String>();
   	 				wsAttr.put("profilesrc", profile_src);
   	 				wsAttr.put("subject", subject);
   	 				wsAttr.put("author", author);

	   	 			}
	   	 		}catch(Exception e){
	   	 			System.out.print("bad!");
	   	 			e.printStackTrace();
	   	 		}
	   	 		finally{
		 			results.close();
	   	 	}
   	 	}catch(Exception e){
	   	 		 e.printStackTrace();}
   	 	finally{
			conn.close();

	   	 }


		return wsAttr;

	}


	private ArrayList<Map<String, String>> getLinks(Repository rep)throws RepositoryException{

		/*
		 * 1. get all channels' policy and profile uris
		 * 2. pass to getPolicy and get Profile the URI
		 * 3. return a list of each policy setting and profile settings
		 */
		RepositoryConnection conn = rep.getConnection();
		ArrayList<Map<String, String>>links = new ArrayList<Map<String, String>>();


   	 	String query_profile_policy = "SELECT channel, profile,policy "
	   		+ "FROM {channel} rdf:type {rss:channel};"
	   		+ "        ws:profile {profile}; "
	   		+ "        cc:morePermissions {policy} "
	   		+ "USING NAMESPACE "
	   		+ "  cc = <http://creativecommons.org/ns#>, "
	   		+ "  rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, "
	   		+ "  ws = <http://foolme.csail.mit.edu/ns/ws#>, "
	   		+ "  rss = <http://purl.org/rss/1.0/>";

   	 	try{
	   	 	TupleQueryResult results = conn.prepareTupleQuery(QueryLanguage.SERQL, query_profile_policy.toString()).evaluate();

	   	 		try{
	   	 			while(results.hasNext()){

	   	 			BindingSet bindings = results.next();
   	 				String channel= bindings.getValue("channel").toString();
   	 				String profile = bindings.getValue("profile").toString();
   	 				String policy= bindings.getValue("policy").toString();

//   	 				logger.info("channel:{}",channel);
//   	 				logger.info("profile:{}",profile);
//   	 				logger.info("policy:{}",policy);

   	 				Map<String, String> wsAttr = new HashMap<String, String>();
   	 				wsAttr.put("channel", channel);
   	 				wsAttr.put("profile", profile);
   	 				wsAttr.put("policy", policy);


   	 				links.add(wsAttr);
	   	 			}
	   	 		}catch(Exception e){
	   	 			System.out.print("bad!");
	   	 			e.printStackTrace();
	   	 		}
	   	 		finally{
		 			results.close();
	   	 	}
   	 	}catch(Exception e){
	   	 		 e.printStackTrace();}
   	 	finally{
			conn.close();

	   	 }

   	 	return links;

	}
	public void validateSource(Repository rep)throws RepositoryException{
		/*
		 * Query source's provenance
		 * TODO: Implement querying source's provenance
		 */
	}

	public void validate(Repository rep)throws RepositoryException{
		boolean conflict;

		//Validate sources, and see if there's any conflicts exist aganist each other

		logger.info("Start validating");
		rdflogger.log("INFO","Start validating");
		// A list of map that has links,policy and profile, of each channel
		ArrayList<Map<String, String>> links = null;

 		try{

			links = getLinks(rep);

		}catch(Exception e){
			 e.printStackTrace();
		}

		//retrieve all profiles

		try{
			for (int i=0 ; i < links.size(); i++){
				 String profileURL = links.get(i).get("profile");
				 wsprofiles.add(getProfile(profileURL));
			}

		}catch(Exception e){
			 e.printStackTrace();

		}

		//retrieve each policy and compare with wsprofiles, write log if conflict happen
		try{
			for (int i=0 ; i < links.size(); i++){

				Map<String,String> linkMap = links.get(i);
				String channel = linkMap.get("channel");
				String PolicyURL = linkMap.get("policy");
				logger.info("Now getting channel:{}",channel);
				rdflogger.log("INFO", "Now getting channel:"+channel);

				Map<String, String> policy = getPolicy(PolicyURL);
				logger.info("Checking policy conflict of {}", channel);
				rdflogger.log("INFO","Checking policy conflict of "+ channel);

				conflict = checkConflict(channel,policy, wsprofiles);
				if (conflict){
					logger.info("[Error]Some merged web service is conflicted with the policy of channel:{}",channel);
					rdflogger.log("POLICY","[Error]Some merged web service is conflicted with the policy of channel:"+channel);
				}
				else{
					logger.info("[Success]All ws sources comply with the policy of channel:{}",channel);
					rdflogger.log("POLICY", "[Success]All ws sources comply with the policy of channel:"+channel);
				}

			}
			//because this is policy aware, we don't restrict designer from merging conflicted ws, but
			//but provide awareness


		}catch(Exception e){

			 e.printStackTrace();
		}

	}

	private boolean checkConflict(String channel, Map<String,String> policy, ArrayList<Map<String, String>> wsprofiles){
		// one policy against all profiles
		boolean conflict = false;
		String rejectSubject = policy.get("rejectSubject");
		String rejectPerson = policy.get("rejectPerson");
		logger.info("Check conflicts, it will reject subject w/{}, and person:{}",rejectSubject,rejectPerson);
		rdflogger.log("INFO", "Check conflicts, it will reject subject w/"+rejectSubject+", and person:"+rejectPerson);
		//chech subject
		for(int i = 0; i<wsprofiles.size();i++){
			Map<String, String> profile = wsprofiles.get(i);
			//logger.info("[DEBUG]subject:{},author:{}",profile.get("subject"),profile.get("author"));
			if(rejectSubject.equals(profile.get("subject")))
			{
				logger.info("[Alert]Profile of {} is conflicted with policy:{}", profile.get("profilesrc"), policy.get("policysrc"));
				rdflogger.log("POLICY", "[Alert]Profile of "+profile.get("profilesrc")+" is conflicted with policy:"+policy.get("policysrc"));
				logger.info("[Alert]Rejecting profile with subject:{}", profile.get("subject"));
				rdflogger.log("POLICY", "[Alert]Rejecting profile with subject:"+ profile.get("subject"));
				conflict = true;

			}
			if(rejectPerson.equals(profile.get("author")))
			{
				logger.info("[Alert]Profile of {} is conflicted with policy :{}", profile.get("profilesrc"),policy.get("policysrc"));
				rdflogger.log("POLICY", "[Alert]Profile of "+ profile.get("profilesrc")+" is conflicted with policy :"+ policy.get("policysrc"));
				logger.info("[Alert]Rejecting profile with author:{}", profile.get("author"));
				rdflogger.log("POLICY","[Alert]Rejecting profile with author:"+profile.get("author"));
				conflict = true;

			}

		}
		return conflict;

	}

	private boolean checkMyConflict(String channel, Map<String,String> policy, Map<String, String> myprofile){

		boolean conflict = false;
		String rejectSubject = policy.get("rejectSubject");
		String rejectPerson = policy.get("rejectPerson");
		logger.info("check conflicts, it will reject subject w/{}, and person:{}",rejectSubject,rejectPerson);
		rdflogger.log("INFO", "check conflicts, it will reject subject w/"+rejectSubject+"and person:"+ rejectPerson);

		//chech subject

			//logger.info("[DEBUG]subject:{},author:{}",profile.get("subject"),profile.get("author"));
			if(rejectSubject.equals(myprofile.get("subject")))
			{
				logger.info("[Alert]Profile of {} is conflicted with policy:{}", myprofile.get("profilesrc"), policy.get("policysrc"));
				rdflogger.log("POLICY", "[Alert]Profile of "+myprofile.get("profilesrc")+ "is conflicted with policy:"+ policy.get("policysrc"));
				logger.info("[Alert]Rejecting profile with subject:{}", myprofile.get("subject"));
				rdflogger.log("POLICY","[Alert]Rejecting profile with subject:"+myprofile.get("subject"));
				conflict = true;

			}
			if(rejectPerson.equals(myprofile.get("author")))
			{
				logger.info("[Alert]Profile of {} is conflicted with policy :{}", myprofile.get("profilesrc"),policy.get("policysrc"));
				rdflogger.log("POLICY", "[Alert]Profile of "+myprofile.get("profilesrc")+ "is conflicted with policy:"+ policy.get("policysrc"));
				logger.info("[Alert]Rejecting profile with author:{}", myprofile.get("author"));
				rdflogger.log("POLICY", "[Alert]Rejecting profile with author:"+ myprofile.get("author"));
				conflict = true;

			}

		return conflict;

	}

    private ArrayList<Statement> asList(RepositoryResult<Statement> iter) throws RepositoryException {
    	ArrayList<Statement> result = new ArrayList<Statement>();
    	while (iter.hasNext()) {
			result.add(iter.next());
		}
    	return result;
    }
	public void merge(Repository rep)throws RepositoryException,RDFParseException,IOException{
		/*
		 * 1. pull your own profile and policy then compare with the rest of the ws, if there's no conflict,
		 * 2. remove source policies, then add the merged policy into the repository, p_merged = p1 + p2 + p3
		 * 3. assert provenance
		 */


		boolean conflict= false;
		RepositoryConnection conn = rep.getConnection();


		//Validate sources, and see if there's any conflicts exist
		logger.info("Start merging my policy");
		rdflogger.log("INFO","Start merging my policy");
		ArrayList<Map<String, String>> links = null;
		Map<String, String> myLinkMap = null;

		/*
		 * first check your policy with the rest of the profile
		 * second check your profile with the rest of the policy
		 */

		//need a temp repository to store your policy and profile

		Repository my_rep = new SailRepository(new MemoryStore()); // create a new memory repository for fetching my rss data
		my_rep.initialize();
		RepositoryConnection my_conn = my_rep.getConnection();
		try{

			my_conn.add(myPolicyRDF, null, RDFFormat.RDFXML);

		}catch(Exception e){

			e.printStackTrace();
		}

 		try{
 			// my policy against all ws profile
			myLinkMap = getLinks(my_rep).get(0); //buggy now, but only one map exist in the arraylist
			String myChannel = myLinkMap.get("channel");
			logger.info("Getting my policy from :{}", myLinkMap.get("policy"));
			rdflogger.log("INFO", "Getting my policy from :"+ myLinkMap.get("policy"));
			Map<String, String> myPolicy = getPolicy(myLinkMap.get("policy"));
			Map<String, String> myProfile = getProfile(myLinkMap.get("profile"));

			logger.info("Checking all profiles against my policy of channel: {}", myChannel);
			rdflogger.log("INFO","Checking all profiles against my policy of channel: "+ myChannel);
			conflict = checkConflict(myChannel,myPolicy, wsprofiles);

			//all other policies of the ws sources, against my profile
			links = getLinks(rep);
			for (int i=0 ; i < links.size(); i++){

				Map<String,String> linkMap = links.get(i);
				String channel = linkMap.get("channel");
				String PolicyURL = linkMap.get("policy");
				Map<String, String> policy = getPolicy(PolicyURL);
				logger.info("Checking policy conflict of {}", channel);
				rdflogger.log("INFO", "Checking policy conflict of "+channel);

				conflict = checkMyConflict(channel,policy,myProfile);
				if (conflict){
					logger.info("[Error]Your service is conflicted with the policy of channel:{}",channel);
					rdflogger.log("POLICY", "[Error]Your service is conflicted with the policy of channel: "+channel);
				}
				else{
					logger.info("[Success]Your service comply with the policy of channel:{}",channel);
					rdflogger.log("POLICY","[Success]Your service comply with the policy of channel:"+channel);
				}


			}

		}catch(Exception e){
			e.printStackTrace();
		}

		if(!conflict)
		{
			try{
				/*
				 * 1. add body of my rss file into the repository(in which has the policy and profile links)
				 * 2. lastly, we can do a demo of sparql query on the new merged RSS
				 */

				logger.info("No Conflict, adding my triples to the repository");
				rdflogger.log("INFO", "No Conflict, adding my triples to the repository");
				ArrayList<Statement> info = asList(my_conn.getStatements(null, null,null,true));
				for (Statement statement : info) {
					conn.add(statement);
					/*
					 * so far only add your policy into the current repository, we should remove the source policy and only
					 * leaves the integrated-policy after merge
					 * TODO: remove previous policy from source, integrate, and add back to the repository the merged policy
 					 *
					 */}


			}
			catch(Exception e){
				e.printStackTrace();

			}
		}
	}

	private void removeSrcPolicy(){
		//TODO: remove the source policy links in the repository

	}


	public void addProvenance(Repository rep)throws RepositoryException,QueryEvaluationException,MalformedQueryException{
		/*
		 * Add provenance information to the repository, including the following
		 * 1. All Sparql queries taken within the configuration (e.g construct & select)
		 * 2. Sources description
		 * 3. Timestamp of generating this provenance
		 * 4. Compliant result (compliant or non-compliant)
		 */
		rdflogger.log("INFO", "Adding provenance assertion");
   	 	RepositoryConnection con = rep.getConnection();
		List<String> sparqls = new LinkedList();
		List<String> sources = new LinkedList();

		sparqls = getTagValuesFromLog(rep, "SPARQL");
		sources = getTagValuesFromLog(rep,"LOCATION");
		ValueFactory f = rep.getValueFactory();
		URI proAnnotation = f.createURI("http://foolme.csail.mit.edu/ns/provenance#annotation");
		// TODO, this resource should of provenance should like the policy-awaer log, have timestamp attached to the end
		// e.g. http://foolme.csail.mit.edu/policy/provenance/pro2009-07-17#16-12-43_418
		URI pro = f.createURI("http://example.com/policy/proveance");

   	 	String provenanceNS = "http://foolme.csail.mit.edu/ns/provenance#";
   	 	URI sparql = f.createURI(provenanceNS, "sparql");
   	 	URI output = f.createURI(provenanceNS, "outpout");
   	 	URI source = f.createURI(provenanceNS, "source");
   	 	URI compliance = f.createURI(provenanceNS, "compliant-with");
   	    URI noncompliance = f.createURI(provenanceNS, "non-compliant-with");
   	 	URI timestamp = f.createURI(provenanceNS, "timestamp");
   	 	URI endPoint = f.createURI("http://example.com/policy/someEndpoint/");


   			//this method use an util class that parse java date to xml datetime, using SimpleDateFormat

   		Calendar cal = Calendar.getInstance();
   		DateToXsdDatetimeFormatter xdf = new DateToXsdDatetimeFormatter();
   		Literal time = f.createLiteral(xdf.format(cal.getTime()));


   	 	con.add(pro, RDF.TYPE,proAnnotation);
   	 	con.add(pro,output,endPoint);
   	 	con.add(pro,timestamp,time);

   	 	for (String s :sparqls){
   	 		Literal sparqlString = f.createLiteral(s);
   	 		System.out.println(s);
   	 		con.add(pro,sparql,sparqlString);
     	}
   	 	for (String sourceURI: sources){
   	 		URI somesource = f.createURI(sourceURI);
   	 		System.out.println(sourceURI);
   	 		con.add(pro,source, somesource);

   	 	}
   	 	//TODO:Should generate policy compliance information as well in the provenance

   	 	// con.add(pro,compliance,)
   	 	con.close();

	}


	private List<String> getTagValuesFromLog(Repository rep, String tagname)throws RepositoryException, MalformedQueryException,QueryEvaluationException{

		List<String> tagValues = new LinkedList() ;
		RepositoryConnection con = rep.getConnection();

   	 	String query_log = "SELECT type,message "
	   		+ "FROM {log} rdf:type {pal:log};"
	   		+ "        pal:type {type}; "
	   		+ "		   pal:timestamp {timestamp}; "
	   		+ "        pal:message {message} "
//	   		+ "        pal:srcclass {srcclass};"
//	   		+ "        pal:srcfunction {srcfunction} "
	   		+ "WHERE type = \""+ tagname+"\" "
	   		+ "USING NAMESPACE "
	   		+ "  rdf = <http://www.w3.org/1999/02/22-rdf-syntax-ns#>, "
	   		+ "  pal = <http://foolme.csail.mit.edu/ns/policyawarelog#> ";
		try{


		TupleQueryResult results = con.prepareTupleQuery(QueryLanguage.SERQL, query_log).evaluate();

		try{
			while(results.hasNext()){
	 				BindingSet testBindings = results.next();
  	 				String type = testBindings.getValue("type").toString();
  	 				String message = testBindings.getValue("message").toString();
  	 				tagValues.add(message);

 			}


		}catch(Exception e){
	 			System.out.print("something bad happen!");
	 			e.printStackTrace();
	 		}finally{
	 			results.close();
	 		}

		}catch(Exception e){
	   	 		 e.printStackTrace();}

		return tagValues;

	}










}
