package com.springsource.insight.plugin.jsf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.springsource.insight.intercept.application.ApplicationName;
import com.springsource.insight.intercept.endpoint.EndPointAnalysis;
import com.springsource.insight.intercept.operation.Operation;
import com.springsource.insight.intercept.operation.OperationType;
import com.springsource.insight.intercept.operation.SourceCodeLocation;
import com.springsource.insight.intercept.trace.Frame;
import com.springsource.insight.intercept.trace.FrameBuilder;
import com.springsource.insight.intercept.trace.SimpleFrameBuilder;
import com.springsource.insight.intercept.trace.Trace;
import com.springsource.insight.intercept.trace.TraceId;

public class JSFEndPointAnalyzerTest {

	private ApplicationName app = ApplicationName.valueOf("app");
	private JSFActionEndPointAnalyzer endPointAnalyzer;
	private Operation actionExecuteOp;

	@Before
	public void setUp() {
		//endPointAnalyzer = new JSFActionEndPointAnalyzer();
		endPointAnalyzer = JSFActionEndPointAnalyzer.getInstance();
		actionExecuteOp = new Operation()
				.type(ActionListenerOperationCollectionAspect.TYPE);
	}

	@Test
	public void locateEndPoint_noHttp() {
		FrameBuilder b = new SimpleFrameBuilder();
		b.enter(new Operation());
		b.enter(actionExecuteOp);
		b.exit();
		Frame rootFrame = b.exit();
		Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), rootFrame);
		assertNull(endPointAnalyzer.locateEndPoint(trace));
	}

	@Test
	public void locateEndPoint_noJSF() {
		FrameBuilder b = new SimpleFrameBuilder();
		Operation httpOp = new Operation().type(OperationType.HTTP);
		b.enter(httpOp);
		b.enter(new Operation());
		b.exit();
		Frame httpFrame = b.exit();
		Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
		assertEquals(null, endPointAnalyzer.locateEndPoint(trace));
	}

	@Test
	public void locateEndPoint_httpMustComeBeforeAccount() {
		FrameBuilder b = new SimpleFrameBuilder();
		b.enter(actionExecuteOp);
		Operation httpOp = new Operation().type(OperationType.HTTP);
		b.enter(httpOp);
		b.exit();
		Frame jsfFrame = b.exit();
		Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), jsfFrame);
		assertEquals(null, endPointAnalyzer.locateEndPoint(trace));
	}

	@Test
	public void locateEndPoint() {
		actionExecuteOp.label("personBean#calculate")
			.put("implementationClass", "javax.faces.event.ActionListener")
			.put("implementationClassMethod", "ProcessAction")
				.sourceCodeLocation(
						new SourceCodeLocation(
								"javax.faces.event.ActionListener",
								"ProcessAction", 111));
		FrameBuilder b = new SimpleFrameBuilder();
		Operation httpOp = new Operation().type(OperationType.HTTP);
		b.enter(httpOp);
		b.enter(actionExecuteOp);
		b.exit();
		Frame httpFrame = b.exit();
		Trace trace = Trace.newInstance(app, TraceId.valueOf("0"), httpFrame);
		EndPointAnalysis endPoint = endPointAnalyzer.locateEndPoint(trace);
		assertEquals("javax.faces.event.ActionListener.ProcessAction", endPoint
				.getEndPointName().getName());
		assertEquals("javax.faces.event.ActionListener#ProcessAction", endPoint.getResourceLabel());
	}

}
