/*
 * #%L
 * konig-web
 * %%
 * Copyright (C) 2015 Gregory McFall
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
$(function(){
	
/***********************************************************************/
/**
 * An array-like object that aggregates a collection of actions so that
 * they can be executed as a group.
 * @param {...Action} action Argument list of actions to be added to this CompositeAction.
 */
function CompositeAction() {
	this.length = 0;
	for (var i=0; i<arguments.length; i++) {
		this.append(arguments[i]);
	}
}

/**
 * Append an action to this composite.
 */
CompositeAction.prototype.append = function(action) {
	this[this.length++] = action;
}

CompositeAction.prototype.push = CompositeAction.prototype.append;

/**
 * Execute the redo operation on each action within this composite.
 */
CompositeAction.prototype.redo = function() {
	for (var i=0; i<this.length; i++) {
		this[i].redo();
	}
}

/**
 * Execute the undo operation on each action within this composite.
 */
CompositeAction.prototype.undo = function() {
	for (var i=this.length-1; i>=0; i--) {
		this[i].undo();
	}
}	

/*****************************************************************************/
function HistoryManager(eventManager) {
	this.eventManager = eventManager;
	this.index = 0;
	this.actionList = [];
	this.currentTransaction = null;
}

/**
 * Begin a new transaction.  Subsequent actions appended to this HistoryManager
 * will be added to a CompositeAction until the commit or abort method is 
 * invoked.
 * @return {TransactionKey} An opaque key that identifies the current transaction, or null 
 * if a transaction is currently in progress. This key
 * is passed back to the HistoryManager when the transaction is committed or aborted.
 */
HistoryManager.prototype.beginTransaction = function() {
	if (this.currentTransaction) {
		return null;
	}
	return this.currentTransaction = new CompositeAction();
}

/**
 * Commit a transaction.
 * @param {OpaqueKey} transactionKey An opaque key for the transaction that is
 * to be committed.  The value may be null, in which case this method does nothing.
 * @return {CompositeAction} A composite action that encapsulates all of the
 *  actions within the transaction.
 */
HistoryManager.prototype.commit = function(transactionKey) {
	var result = this.currentTransaction;
	if (transactionKey) {
		if (transactionKey != this.currentTransaction) {
			// TODO: raise an event to handle the error more gracefully.
			console.log("Aborting invalid transaction.");
			if (this.currentTransaction) {
				this.abort();
			}
		}
		this.currentTransaction = null;
		if (transactionKey.length>0) {
			this.append(transactionKey);
		}
	}
	return result;
}

/**
 *  Abort a transaction. The actions collected within the transaction to this point
 *  will be undone as a side-effect.
 *  @param {OpaqueKey} transactionKey An opaque key for the transaction that is to
 *  be aborted. 
 */
HistoryManager.prototype.abort = function(transactionKey) {
	if (transactionKey) {
		if (transactionKey != this.currentTransaction) {
			// TODO: raise an event to handle the error more gracefully.
			console.log("Invalid transaction key; aborting anyway");
		}
		this.currentTransaction.undo();
		this.currentTransaction = null;
	}
}

HistoryManager.prototype.peek = function() {
	return this.actionList.length>0 ? this.actionList[this.actionList.length-1] : null;
}

HistoryManager.prototype.append = function(action) {

	if (this.currentTransaction) {
		this.currentTransaction.append(action);
		return;
	}
	
	if (this.index <this.actionList.length) {
		this.actionList.splice(this.index, this.actionList.length - this.index);
	}
	
	// Collect any side-effects in a transaction
	this.currentTransaction = (action instanceof CompositeAction) ? action : new CompositeAction(action);

	if (this.eventManager) {
		this.eventManager.notifyHandlers(action);
	}
	
	if (this.currentTransaction === action || this.currentTransaction.length===1) {
		this.actionList.push(action);
	
	} else {
		this.actionList.push(this.currentTransaction);
	}
	this.currentTransaction = null;
	
	this.index = this.actionList.length;
}

HistoryManager.prototype.undo = function() {
	if (this.index>0) {
		this.index--;
	} else {
		return;
	}
	if (this.index<this.actionList.length) {
		this.actionList[this.index].undo();
	}
}

HistoryManager.prototype.redo = function() {
	if (this.index<this.actionList.length) {
		this.actionList[this.index++].redo();
	}
}


/*****************************************************************************/

konig.HistoryManager = HistoryManager;
konig.CompositeAction = CompositeAction;

});
