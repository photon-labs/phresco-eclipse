package com.photon.phresco.ui.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * @author suresh_ma
 *
 */
public class CustomItemList extends Composite {

	/**
	 * 
	 */
	private List<Object> elements = new ArrayList<Object>();
	
	/**
	 * 
	 */
	private CustomListLabelDecorator decorator;
	
	/**
	 * @param parent
	 * @param style
	 */
	public CustomItemList(Composite parent, int style) {
		super(parent, style);
		RowLayout layout = new RowLayout();
		layout.wrap=true;
		setLayout(layout);
	}

	/**
	 * @return the elements
	 */
	public List<Object> getElements() {
		return elements;
	}

	/**
	 * @param elements the elements to set
	 */
	public void setElements(List<Object> elements) {
		this.elements = elements;
		refresh();
	}
	
	/**
	 * @param decorator the decorator to set
	 */
	public void setDecorator(CustomListLabelDecorator decorator) {
		this.decorator = decorator;
	}
	
	public void addElement(List<Object> objectList) {
		for (Object object : objectList) {
			elements.add(object);
			createControlsFor(object);
			this.getParent().layout(true);
		}
	}
	
	/**
	 * 
	 */
	public void refresh() {
		Control[] children = getChildren();
		for (Control control : children) {
			control.dispose();
		}
		for (Object element : elements) {
			createControlsFor(element);
		}
	}

	private void createControlsFor(Object element) {
		Composite elementComposite = new Composite(this, SWT.None);
//		Composite group = new Composite(elementComposite, SWT.NONE);
		elementComposite.setLayout(new GridLayout(2, false));
		Label label = new Label(elementComposite, SWT.NONE);
		label.setText(decorator.getdisplayName(element));
		Button removeButton = new Button(elementComposite, SWT.NONE);
		removeButton.setData(element);
		removeButton.setText("X");
		layout(true);
//		elementComposite.setContent(group);
//		elementComposite.setExpandHorizontal(true);
//		elementComposite.setExpandVertical(true);
		removeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button button = (Button) e.getSource();
				elements.remove(button.getData());
				button.getParent().dispose();
				layout(true);
			}
		});
	}
}
