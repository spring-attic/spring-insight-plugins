package com.springsource.insight.plugin.jsf;

import java.util.List;

import com.springsource.insight.intercept.endpoint.AbstractSingleTypeEndpointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.endpoint.EndPointAnalyzer;
import com.springsource.insight.intercept.endpoint.EndPointName;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationMap;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameUtil;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.util.StringUtil;

// public class JSFActionEndPointAnalyzer implements EndPointAnalyzer {
public class JSFActionEndPointAnalyzer extends AbstractSingleTypeEndpointAnalyzer {
	
	private static final JSFActionEndPointAnalyzer INSTANCE = new JSFActionEndPointAnalyzer();
	private JSFActionEndPointAnalyzer() {
		super(OperationType.valueOf("jsf_action_listener_operation"));
	}
	public static final JSFActionEndPointAnalyzer getInstance() {
		return INSTANCE;
	}
//	@Override
//	public EndPointAnalysis locateEndPoint(Trace trace) {
//		Frame lifecycleExecuteFrame = trace
//				.getFirstFrameOfType(OperationType.valueOf("jsf_action_listener_operation"));
//		if (lifecycleExecuteFrame == null) {
//			return null;
//		}
//
//		Frame httpFrame = trace.getFirstFrameOfType(OperationType.HTTP);
//		if (httpFrame == null
//				|| !FrameUtil.frameIsAncestor(httpFrame, lifecycleExecuteFrame)) {
//			return null;
//		}
//
//		Operation operation = lifecycleExecuteFrame.getOperation();
//
//		String resourceKey = operation.get("implementationClass") + "."
//				+ operation.get("implementationClassMethod");
//		String resourceLabel = operation.get("implementationClass") + "#"
//				+ operation.get("implementationClassMethod");
//
//		Operation httpOperation = httpFrame.getOperation();
//		String exampleRequest = httpOperation.getLabel();
//		int score = FrameUtil.getDepth(lifecycleExecuteFrame);
//		//return new EndPointAnalysis(trace.getRange(),
//		//		EndPointName.valueOf(resourceKey), resourceLabel,
//		//		exampleRequest, score);
//		return new EndPointAnalysis(EndPointName.valueOf(resourceKey), resourceLabel,
//				exampleRequest, score);
//	}
	@Override
	public Frame getScoringFrame(Trace trace) {
		for (OperationType type : getOperationTypes()) {
			Frame	frame=trace.getFirstFrameOfType(type);
			if (frame != null) {
				if (validateFrameParent(frame, OperationType.HTTP) == null) {
					return null;
				} else {
					return frame;
				}
			}
		}

		return null;	// no match
	}
	@Override
	protected OperationType validateScoringFrame (Frame frame) {
		OperationType	opType=validateFrameOperation(frame);
		if (opType == null) {	// not one of the valid frames
			return null;
		}

		if (validateFrameParent(frame, OperationType.HTTP) == null) {
			return null;
		} else {
			return opType;
		}
	}
    @Override
    protected EndPointAnalysis makeEndPoint(Frame lifecycleExecuteFrame, int depth) {
    	
    	Frame httpFrame = FrameUtil.getFirstParentOfType(lifecycleExecuteFrame, OperationType.HTTP);
    	Operation operation = lifecycleExecuteFrame.getOperation();
    	
    	String impClasString = (String) operation.get("implementationClass");
    	String impClassMethodString = (String) operation.get("implementationClassMethod");
    	String resourceKey = impClasString + "." + impClassMethodString;
    	String resourceLabel = impClasString + "#" + impClassMethodString;

    	String example=null;
    	if (httpFrame != null) {
    		Operation op = httpFrame.getOperation();
    		OperationMap request = op.get("request", OperationMap.class);
    		example=EndPointAnalysis.createHttpExampleRequest(request);
    		if (StringUtil.isEmpty(example)) {
    			example = op.getLabel();
    		}
    	} else 
    		example = "unknown";
        return new EndPointAnalysis(EndPointName.valueOf(resourceKey), resourceLabel, example, getOperationScore(operation, depth), operation);
    }

}
