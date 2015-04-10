package org.japura.gui;

import org.japura.gui.event.ButtonTextFieldEvent;
import org.japura.gui.event.ButtonTextFieldListener;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.CaretListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.LinkedHashMap;

/**
 * Copyright (C) 2010-2015 Carlos Eduardo Leite de Andrade
 * <P>
 * This library is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * <P>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <P>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <A
 * HREF="www.gnu.org/licenses/">www.gnu.org/licenses/</A>
 * <P>
 * For more information, contact: <A HREF="www.japura.org">www.japura.org</A>
 * <P>
 * 
 * @author Carlos Eduardo Leite de Andrade
 */
public class ButtonTextField extends JPanel{

  private static final long serialVersionUID = 2L;

  private EventListenerList listeners = new EventListenerList();

  private JTextField field;
  private IconLabel icon;
  private IconLabel selectedDropDownIcon;
  private JPanel dropDownButtonPanel;
  private JLabel dropDownButton;
  private LinkedHashMap<String, Action> actions;
  private String currentButton;
  private MouseAdapter popupMouseAdapter;
  private JPopupMenu actionsChooser;
  private JPanel actionsRoot;
  private DefaultListModel listModel;
  private JList actionsList;
  private ListRenderer listRenderer;

  private Icon fixedIcon;
  private int maxWidth;
  private int maxHeight;

  private static int marginGap = 3;

  public ButtonTextField(String text) {
	this(new JTextField(text));
	updateMargin();
  }

  public ButtonTextField(int columns) {
	this(new JTextField(columns));
	updateMargin();
  }

  public ButtonTextField(String text, int columns) {
	this(new JTextField(text, columns));
	updateMargin();
  }

  public ButtonTextField() {
	this(new JTextField());
	updateMargin();
  }

  public ButtonTextField(JTextField field) {
	if (field == null) {
	  field = new JTextField();
	}
	this.field = field;
	setBorder(field.getBorder());
	field.setBorder(null);

	field.addPropertyChangeListener(new PropertyChangeListener() {
	  @Override
	  public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals("margin")) {
		  Insets margin = getField().getMargin();
		  if (margin == null) {
			getField().setBorder(null);
		  } else {
			Border border =
				BorderFactory.createEmptyBorder(margin.top, margin.left,
					margin.bottom, margin.right);
			getField().setBorder(border);
		  }
		}
	  }
	});
	updateMargin();

	actions = new LinkedHashMap<String, Action>();

	setBackground(Color.WHITE);
	setOpaque(true);
	setLayout(new GridBagLayout());

	GridBagConstraints gbc = new GridBagConstraints();
	gbc.gridx = 0;
	gbc.gridy = 0;
	gbc.fill = GridBagConstraints.VERTICAL;
	gbc.weighty = 1;
	add(getDropDownButtonPanel(), gbc);

	gbc.gridx = 1;
	gbc.gridy = 0;
	gbc.weighty = 0;
	gbc.weightx = 1;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	add(getField(), gbc);

	gbc.gridx = 2;
	gbc.gridy = 0;
	gbc.weightx = 0;
	add(getIcon(), gbc);
  }

  public void addButtonTextFieldListener(ButtonTextFieldListener listener) {
	listeners.add(ButtonTextFieldListener.class, listener);
  }

  public void removeButtonTextFieldListener(ButtonTextFieldListener listener) {
	listeners.remove(ButtonTextFieldListener.class, listener);
  }

  private void updateMargin() {
	getField().setMargin(new Insets(0, marginGap, 0, marginGap));
  }

  public Icon getFixedIcon() {
	return fixedIcon;
  }

  public void setFixedIcon(Icon fixedIcon) {
	this.fixedIcon = fixedIcon;
	if (getFixedIcon() != null) {
	  getIcon().setIcon(getFixedIcon());
	} else {
	  if (icon != null) {
		Action action = actions.get(currentButton);
        Icon icon = action.getIcon();
		getIcon().setIcon(icon);
	  } else {
		getIcon().setIcon(null);
	  }
	}
	updateMaxSizes();
  }

  public boolean isDropDownVisible() {
	return getDropDownButtonPanel().isVisible();
  }

  public void setDropDownVisible(boolean visible) {
	getDropDownButtonPanel().setVisible(visible);
	updateDropDownIconBorder();
  }

  @Override
  public Dimension getPreferredSize() {
	if (isPreferredSizeSet()) {
	  return super.getPreferredSize();
	}

	Dimension dim = new Dimension();

	// field
	Dimension fieldDim = field.getPreferredSize();
	Insets fieldInsets = field.getInsets();

	dim.width += fieldDim.width + fieldInsets.left + fieldInsets.right;
	dim.height += fieldDim.height + fieldInsets.top + fieldInsets.bottom;

	// options
	if (isDropDownVisible()) {
	  Dimension dropDownDim = getDropDownButtonPanel().getPreferredSize();
	  Insets dropDownInsets = getDropDownButtonPanel().getInsets();

	  int dropDownHeight =
		  dropDownDim.height + dropDownInsets.top + dropDownInsets.bottom;
	  dim.width +=
		  dropDownDim.width + dropDownInsets.left + dropDownInsets.right;
	  dim.height = Math.max(dim.height, dropDownHeight);
	}

	// icon
	if (isIconVisible()) {
	  if (getFixedIcon() != null) {
		Dimension iconDim = getIcon().getPreferredSize();
		dim.width += iconDim.width;
		dim.height = Math.max(dim.height, iconDim.height);
	  } else {
		Insets iconInsets = getIcon().getInsets();
		int iconHeight = maxHeight + iconInsets.top + iconInsets.bottom;
		dim.width += maxWidth + iconInsets.left + iconInsets.right;
		dim.height = Math.max(dim.height, iconHeight);
	  }
	}

	// border
	Insets insets = getInsets();

	dim.width += insets.left + insets.right;
	dim.height += insets.bottom + insets.top;

	return dim;
  }

  public void fireCurrentButtonActionListeners() {
	if (currentButton != null && isEnabled()) {
	  Action action = actions.get(currentButton);
	  ActionListener listener = action.getListener();
	  if (listener != null) {
		listener.actionPerformed(new ActionEvent(this,
			ActionEvent.ACTION_PERFORMED, ""));
	  }
	}
  }

  private void showButtonsChooser() {
	if (isEnabled()) {
	  Dimension dim = getSize();
	  Dimension bcDim = getActionsRoot().getPreferredSize();
	  Insets insets = getActionsChooser().getInsets();
	  int width = dim.width;
	  width = bcDim.width + insets.left + insets.right;
	  int height = bcDim.height + insets.bottom + insets.top;
	  Dimension newDim = new Dimension(width, height);
	  getActionsChooser().setPreferredSize(newDim);
	  getActionsChooser().show(this, 0, dim.height);
	}
  }

  private JPopupMenu getActionsChooser() {
	if (actionsChooser == null) {
	  actionsChooser = new JPopupMenu();
	  actionsChooser.add(getActionsRoot());
	}
	return actionsChooser;
  }

  private JPanel getActionsRoot() {
	if (actionsRoot == null) {
	  actionsRoot = new JPanel();
	  actionsRoot.setLayout(new BorderLayout());
	  actionsRoot.setBorder(BorderFactory.createLineBorder(Color.WHITE, 5));
	  actionsRoot.add(getActionsList(), BorderLayout.CENTER);
	}
	return actionsRoot;
  }

  private DefaultListModel getListModel() {
	if (listModel == null) {
	  listModel = new DefaultListModel();
	}
	return listModel;
  }

  private ListRenderer getListRenderer() {
	if (listRenderer == null) {
	  listRenderer = new ListRenderer();
	}
	return listRenderer;
  }

  private JList getActionsList() {
	if (actionsList == null) {
	  actionsList = new JList();
	  actionsList.setCellRenderer(getListRenderer());
	  actionsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	  actionsList.setModel(getListModel());
	  actionsList.addListSelectionListener(new ListSelectionListener() {
		@Override
		public void valueChanged(ListSelectionEvent e) {
		  if (e.getValueIsAdjusting() == false) {
			if (actionsList.getSelectedIndex() > -1) {
			  actionsList.clearSelection();
			}
		  }
		}
	  });
	  actionsList.addMouseMotionListener(new MouseMotionListener() {
		@Override
		public void mouseDragged(MouseEvent e) {}

		@Override
		public void mouseMoved(MouseEvent e) {
		  listRenderer.mouseOverIndex =
			  actionsList.locationToIndex(e.getPoint());
		  actionsList.repaint();
		}
	  });
	  actionsList.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  String actionName = null;
		  if (listRenderer.mouseOverIndex >= 0
			  && listRenderer.mouseOverIndex < getListModel().size()) {
			actionName =
				(String) getListModel().get(listRenderer.mouseOverIndex);
		  }
		  getActionsChooser().setVisible(false);
		  listRenderer.mouseOverIndex = -1;
		  if (actionName != null) {
			setCurrentButton(actionName);
		  }
		}

		@Override
		public void mouseExited(MouseEvent e) {
		  listRenderer.mouseOverIndex = -1;
		  actionsList.repaint();
		}
	  });
	}
	return actionsList;
  }

  @Override
  public Color getForeground() {
    return getField().getForeground();
  }

  @Override
  public void setForeground(Color fg) {
    getField().setForeground(fg);
  }

  public void addButton(String buttonName, ActionListener listener) {
    addButton(buttonName, null, listener);
  }

  public void addButton(String buttonName, Icon icon, ActionListener listener) {
	Action action = new Action(listener, icon);
	actions.put(buttonName, action);
	getListModel().addElement(buttonName);
	if (currentButton == null) {
	  setCurrentButton(buttonName);
	}
	updateMaxSizes();
	updateDropDownIconBorder();
  }

  private void updateDropDownIconBorder() {
	if (isDropDownVisible()) {
	  for (Action action : actions.values()) {
		if (action.getIcon() != null) {
		  selectedDropDownIcon.setBorder(BorderFactory.createEmptyBorder(2, 0,
			  2, 3));
		  return;
		}
	  }
	}
	selectedDropDownIcon.setBorder(BorderFactory.createEmptyBorder());
  }

  private void updateMaxSizes() {
	maxWidth = 0;
	maxHeight = 0;
	for (Action action : actions.values()) {
	  if (action.getIcon() != null) {
		maxWidth = Math.max(maxWidth, action.getIcon().getIconWidth());
		maxHeight = Math.max(maxHeight, action.getIcon().getIconHeight());
	  }
	}
  }

  public JTextField getField() {
	return field;
  }

  public boolean isIconVisible() {
	return getIcon().isVisible();
  }

  public void setIconVisible(boolean visible) {
	getIcon().setVisible(visible);
  }

  @Override
  public void setEnabled(boolean enabled) {
	super.setEnabled(enabled);
	field.setEnabled(enabled);
	getIcon().setEnabled(enabled);
	getDropDownButton().setEnabled(enabled);
  }

  public void setCurrentButton(String buttonName) {
	Action action = actions.get(buttonName);
	if (action != null) {
	  ButtonTextFieldEvent event =
		  new ButtonTextFieldEvent(currentButton, buttonName,
			  System.currentTimeMillis());
	  for (ButtonTextFieldListener l : listeners
		  .getListeners(ButtonTextFieldListener.class)) {
		l.buttonChanged(event);
	  }

	  this.currentButton = buttonName;
      Icon icon = action.getIcon();
	  if (icon != null) {
		getSelectedDropDownIcon().setIcon(icon);
	  } else {
		getSelectedDropDownIcon().setIcon(null);
	  }
	  if (getFixedIcon() == null) {
		getIcon().setIcon(icon);
	  }
	}
  }

  public String getCurrentButtonName() {
	return currentButton;
  }

  private IconLabel getIcon() {
	if (icon == null) {
	  icon = new IconLabel(true);
	  icon.setCursor(new Cursor(Cursor.HAND_CURSOR));
	  icon.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 2));
	  icon.setBackground(field.getBackground());
	  icon.setOpaque(true);
	  icon.addMouseListener(new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  fireCurrentButtonActionListeners();
		}
	  });
	}
	return icon;
  }

  public void setIconCursor(Cursor cursor) {
	getIcon().setCursor(cursor);
  }

  @Override
  public void setBackground(Color background) {
	super.setBackground(background);
	if (field != null) {
	  field.setBackground(background);
	  getIcon().setBackground(background);
	}
  }

  private MouseAdapter getPopupMouseAdapter() {
	if (popupMouseAdapter == null) {
	  popupMouseAdapter = new MouseAdapter() {
		@Override
		public void mouseClicked(MouseEvent e) {
		  showButtonsChooser();
		}
	  };
	}
	return popupMouseAdapter;
  }

  private JLabel getDropDownButton() {
	if (dropDownButton == null) {
	  URL url = getClass().getResource("/images/jpr_splitbuttondown.png");
	  dropDownButton = new JLabel();
	  dropDownButton.setIcon(new ImageIcon(url));
	  dropDownButton.addMouseListener(getPopupMouseAdapter());
	}
	return dropDownButton;
  }

  public boolean isSelectedDropDownIconVisible() {
	return getSelectedDropDownIcon().isVisible();
  }

  public void setDropDownButtonBackground(Color color) {
	getDropDownButtonPanel().setBackground(color);
  }

  public Color getDropDownButtonBackground() {
	return getDropDownButtonPanel().getBackground();
  }

  public void setSelectedDropDownIconVisible(boolean visible) {
	getSelectedDropDownIcon().setVisible(visible);
  }

  private IconLabel getSelectedDropDownIcon() {
	if (selectedDropDownIcon == null) {
	  selectedDropDownIcon = new IconLabel(false);
	  selectedDropDownIcon.addMouseListener(getPopupMouseAdapter());
	}
	return selectedDropDownIcon;
  }

  private JPanel getDropDownButtonPanel() {
	if (dropDownButtonPanel == null) {
	  dropDownButtonPanel = new JPanel(new BorderLayout());
	  dropDownButtonPanel.addMouseListener(getPopupMouseAdapter());
	  dropDownButtonPanel.add(getSelectedDropDownIcon(), BorderLayout.WEST);

	  dropDownButtonPanel.add(getDropDownButton(), BorderLayout.CENTER);

	  Border out = BorderFactory.createMatteBorder(0, 0, 0, 1, Color.GRAY);
	  Border in = BorderFactory.createEmptyBorder(0, 2, 0, 2);
	  Border botder = BorderFactory.createCompoundBorder(out, in);
	  dropDownButtonPanel.setBorder(botder);
	}
	return dropDownButtonPanel;
  }

  /**
   * @see JTextField#setText(String)
   */
  public void setText(String text) {
	getField().setText(text);
  }

  /**
   * @see JTextField#getText()
   */
  public String getText() {
	return getField().getText();
  }

  /**
   * @see JTextField#setDocument(Document)
   */
  public void setDocument(Document doc) {
	getField().setDocument(doc);
  }

  /**
   * @see JTextField#getDocument()
   */
  public Document getDocument() {
	return getField().getDocument();
  }

  @Override
  public synchronized void addFocusListener(FocusListener listener) {
	getField().addFocusListener(listener);
  }

  /**
   * @see JTextField#addKeyListener(KeyListener)
   */
  public synchronized void addKeyListener(KeyListener listener) {
	getField().addKeyListener(listener);
  }

  /**
   * @see JTextField#addCaretListener(CaretListener)
   */
  public void addCaretListener(CaretListener listener) {
	getField().addCaretListener(listener);
  }

  private static class Action{
	private ActionListener listener;
	private Icon icon;

	public Action(ActionListener listener, Icon icon) {
	  this.listener = listener;
	  if (icon != null) {
		this.icon = icon;
	  }
	}

	protected ActionListener getListener() {
	  return listener;
	}

	protected Icon getIcon() {
	  return icon;
	}

  }

  private class IconLabel extends JLabel{

	private static final long serialVersionUID = 729241592604471900L;
	private boolean fieldIcon;

	public IconLabel(boolean fieldIcon) {
	  super("", null, JLabel.CENTER);
	  this.fieldIcon = fieldIcon;
	}

	@Override
	public Dimension getPreferredSize() {
	  if (fieldIcon) {
		return super.getPreferredSize();
	  }
	  Insets insets = getInsets();
	  int w = insets.left + insets.right + maxWidth;
	  int h = insets.top + insets.bottom + maxHeight;
	  return new Dimension(w, h);
	}
  }

  private class ListRenderer extends JPanel implements ListCellRenderer{

	private static final long serialVersionUID = -7811567885926598910L;
	public int mouseOverIndex;
	private Border mouseOverBorder;
	private Border mouseOutBorder;
	private JLabel label;
	private IconLabel icon;

	public ListRenderer() {
	  label = new JLabel();
	  icon = new IconLabel(false);
	  setLayout(new BorderLayout(2, 0));
	  add(label, BorderLayout.CENTER);
	  add(icon, BorderLayout.WEST);
	  setBackground(Color.WHITE);
	  label.setOpaque(true);
	}

	@Override
	public Component getListCellRendererComponent(JList list, Object value,
												  int index,
												  boolean isSelected,
												  boolean cellHasFocus) {
	  String name = (String) value;
	  label.setText(name);
	  Action action = actions.get(name);
	  if (action.getIcon() != null) {
		icon.setIcon(action.getIcon());
	  } else {
		icon.setIcon(null);
	  }

	  if (index == mouseOverIndex) {
		label.setBorder(getMouseOverBorder());
		label.setBackground(Color.BLACK);
		label.setForeground(Color.WHITE);
	  } else {
		label.setBorder(getMouseOutBorder());
		label.setBackground(Color.WHITE);
		label.setForeground(Color.BLACK);
	  }
	  return this;
	}

	public Border getMouseOverBorder() {
	  if (mouseOverBorder == null) {
		mouseOverBorder = BorderFactory.createLineBorder(Color.BLACK, 3);
	  }
	  return mouseOverBorder;
	}

	public Border getMouseOutBorder() {
	  if (mouseOutBorder == null) {
		mouseOutBorder = BorderFactory.createLineBorder(Color.WHITE, 3);
	  }
	  return mouseOutBorder;
	}

  }

}
