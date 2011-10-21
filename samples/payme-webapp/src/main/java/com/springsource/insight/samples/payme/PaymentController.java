/**
 * Copyright 2009-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.springsource.insight.samples.payme;

import java.util.Random;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * This controller provides a very simple example for use when testing the insight-myplugin scaffold.
 */
@Controller
public class PaymentController {
    private Random rand = new Random();
    private static final String    DEFAULT_DELAY="50";

    public PaymentController () {
        super();
    }

    @RequestMapping("/")
	public ModelAndView paymentHandler(@RequestParam(value="delay", defaultValue=DEFAULT_DELAY) int maxDelay) {
        ModelAndView paymentAmount = new ModelAndView("gotpaid");
        int randomPayment = getRandomPaymentAmount();
        FakeAccount account = getAccount();
        account.setMaxDelay(maxDelay);
        
        // This call to setBalance() is intercepted and recorded by Insight when using
        // the insight-myplugin scaffold.
        account.setBalance(randomPayment);
        
        paymentAmount.addObject("account", account);
		return paymentAmount;
	}
	
	private int getRandomPaymentAmount() {
	    return rand.nextInt(1000) + 30;
	}
	
	private FakeAccount getAccount() {
	    return new FakeAccount();
	}
}
