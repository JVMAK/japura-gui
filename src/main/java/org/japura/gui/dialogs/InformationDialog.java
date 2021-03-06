package org.japura.gui.dialogs;

import org.japura.gui.GUIImages;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import java.awt.Component;
import java.awt.Window;

/**
 * <P>
 * Copyright (C) 2015 Carlos Eduardo Leite de Andrade
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
public class InformationDialog extends AbstractStandardDialog {

  private static Icon defaultDialogIcon = new ImageIcon(GUIImages.INFORMATION);
  private static Icon defaultMessageIcon = new ImageIcon(GUIImages.INFORMATION);

  public static void setDefaultMessageIcon(Icon icon) {
    if (icon != null) {
      InformationDialog.defaultMessageIcon = icon;
    }
  }

  public static void setDefaultDialogIcon(Icon icon) {
    if (icon != null) {
      InformationDialog.defaultDialogIcon = icon;
    }
  }

  public static Icon getDefaultDialogIcon() {
    return InformationDialog.defaultDialogIcon;
  }

  public static Icon getDefaultMessageIcon() {
    return InformationDialog.defaultMessageIcon;
  }

  public static void show(Component owner, String title, String message) {
    InformationDialog dialog = new InformationDialog(title, message);
    dialog.show(owner);
  }

  public static void show(Window owner, String title, String message) {
    InformationDialog dialog = new InformationDialog(title, message);
    dialog.show(owner);
  }

  public static void show(String title, String message) {
    InformationDialog dialog = new InformationDialog(title, message);
    dialog.show();
  }

  public InformationDialog(String title, String message) {
    super(title, message);
    setMessageIcon(InformationDialog.getDefaultMessageIcon());
    if (InformationDialog.getDefaultDialogIcon() != null) {
      setDialogIcon(InformationDialog.getDefaultDialogIcon());
    }
  }
}
