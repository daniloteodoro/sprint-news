package com.sprintnews.application.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

	// Invoked by the dialog's url set on atlassian-connect.json. This will be initially handled by the index.html file inside the
	// templates directory, which bootstraps the React app.
	@RequestMapping(value = "/idx")
	public String index() {
		logger.info("count#setup.invoked=1");
		return "index";
	}

}
