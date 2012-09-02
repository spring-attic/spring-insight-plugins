/**
 * Copyright (c) 2009-2011 VMware, Inc. All Rights Reserved.
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

package com.springsource.insight.plugin.springtx;

import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Transaction management related pointcuts 
 *
 */
public aspect TransactionPointcuts {
    public pointcut transactionBegin(TransactionDefinition txDefinition) 
        : execution(* PlatformTransactionManager.getTransaction(TransactionDefinition)) 
          && args(txDefinition);
        
    public pointcut transactionRollback(TransactionStatus txStatus)
        : execution(* PlatformTransactionManager.rollback(TransactionStatus)) 
          && args(txStatus);
    
    public pointcut transactionCommit(TransactionStatus txStatus)
        : execution(* PlatformTransactionManager.commit(TransactionStatus)) 
          && args(txStatus);
    
    public pointcut transactionalExecution() : 
        execution(@Transactional * *(..)) || execution(* (@Transactional *).*(..));
}
