package com.camunda.training;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

public class CreateTweetDelegate implements JavaDelegate {

	private final Logger LOGGER = LoggerFactory.getLogger(CreateTweetDelegate.class.getName());

	@Override
	public void execute(DelegateExecution execution) throws Exception {
		// TODO Auto-generated method stub
		    //String content = "I did it! Cheers dear ";
		
			String content = (String) execution.getVariable("content");
			
		    LOGGER.info("Publishing tweet: " + content);
		    
		    if (content.equals("Network error")) {
		        throw new RuntimeException("simulated network error");
		      }
		    
		    AccessToken accessToken = new AccessToken("220324559-jet1dkzhSOeDWdaclI48z5txJRFLCnLOK45qStvo", "B28Ze8VDucBdiE38aVQqTxOyPc7eHunxBVv7XgGim4say");
		    Twitter twitter = new TwitterFactory().getInstance();
		    twitter.setOAuthConsumer("lRhS80iIXXQtm6LM03awjvrvk", "gabtxwW8lnSL9yQUNdzAfgBOgIMSRqh7MegQs79GlKVWF36qLS");
		    twitter.setOAuthAccessToken(accessToken);
		    twitter.updateStatus(content);
		  
	}

}
