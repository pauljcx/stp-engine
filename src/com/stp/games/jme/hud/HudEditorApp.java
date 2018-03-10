/* MIT License
 *
 * Copyright (c) 2018 Paul Collins
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.stp.games.jme.hud;
// JME3 Dependencies
import com.jme3.asset.AssetManager;
import com.jme3.app.Application;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.font.BitmapFont;
// Java Dependencies
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
// Internal Dependencies
import com.stp.games.jme.GameRegistry;
import com.stp.games.jme.controls.Schematic;
import com.stp.games.jme.controls.GameControl;
import com.stp.games.jme.controls.ResourceControl;

/** @author Paul Collins
 *  @version v1.0 ~ 10/27/2015
 *  HISTORY: Version 1.0 Created control HudEditorApp ~ 10/27/2015
 */
public class HudEditorApp extends HudContainer {
	private Application app;
	private HudComponent icon;
	private HudComponent idField;
	private HudTextField nameField;
	private HudDropList categoryField;
	private HudDropList typeField;
	private HudDropList modelList;
	private HudTextArea descriptField;
	private HudContainer paramList;
	private HudWindow editWindow;
	private HudWindow schemaWindow;
	private HudDropList categoryList;
	private HudContainer schemaList;
	private HudButton addButton;
	private HudButton saveButton;
	private HudButton addResource;
	private HudButton removeResource;
	private HudItemList resourceList;
	
	private HudWindow resourceWindow;
	private HudDropList resourceCategory;
	private HudDropList resourceStyle;
	private HudTextField resourceDescription;
	private HudTextField resourceQuantity;
	private HudButton resourceSave;
	
	private Schematic editValue;

	public HudEditorApp(String name, Application app) {
		super(name, null, 0, 0);
		this.app = app;
		//setBackground(null);
		setAbsolute();
		setVisible(false);
		
		/*schemaWindow = new HudWindow(name + "-shema-window", (Texture2D)app.getAssetManager().loadTexture("Interface/border96.png"), 600, 384);
		schemaWindow.createNinePartTexture(32);
		schemaWindow.createExitButton((Texture2D)app.getAssetManager().loadTexture("Interface/exit_button.png"), 32, 32, 2, 2);
		schemaWindow.setCentered();
		schemaWindow.setListener(this);
		
		HudContainer schemaDialog = new HudContainer(name + "-schema-dialog", 576, 312);
		schemaDialog.setCentered();		
		categoryList = new HudDropList(name + "-schema-category", 420, 24);
		categoryList.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		categoryList.setListener(this);
		for (Schematic.Category c : Schematic.Category.values()) {
			categoryList.addItem(c.text());
		}
		categoryList.setSelectedIndex(0);
		addButton = new HudButton("New", HudManager.DEFAULT_FONT, (Texture2D)app.getAssetManager().loadTexture("Interface/button.png"), 64, 24);
		addButton.setTextSize(24f);
		addButton.setRelative();
		addButton.setListener(this);
		HudContainer categoryPanel = new HudContainer(name + "-schema-panel0", 576, 24);
		categoryPanel.setRelative();
		categoryPanel.add(new HudLabel("Category:", 92, 24));
		categoryPanel.add(categoryList);
		categoryPanel.add(addButton);
		categoryPanel.setLayoutMode(HORIZONTAL_LIST);
		schemaDialog.add(categoryPanel);
		
		schemaList = new HudContainer(name + "-schema-list", 576, 288);
		schemaList.setRelative();
		schemaList.setLayoutMode(GRID_LIST);
		schemaDialog.add(schemaList);
		schemaDialog.setLayoutMode(VERTICAL_LIST);
		schemaWindow.add(schemaDialog);
		schemaWindow.setVisible(false);
		add(schemaWindow);
		
		editWindow = new HudWindow(name + "-edit-window", (Texture2D)app.getAssetManager().loadTexture("Interface/border96.png"), 920, 680);
		editWindow.createNinePartTexture(32);
		editWindow.createExitButton((Texture2D)app.getAssetManager().loadTexture("Interface/exit_button.png"), 32, 32, 2, 2);
		editWindow.setHorizontalAlignment(CENTERED, 0);
		editWindow.setVerticalAlignment(MIN_JUSTIFY, 0);
		editWindow.setListener(this);

		HudContainer editDialog = new HudContainer(name + "-dialog", 864, 600);
		HudContainer left = new HudContainer(name + "-left", 416, 600);
		left.setRelative();		
		editDialog.add(left);
		HudContainer center = new HudContainer(name + "-center", 192, 600);
		center.setRelative();
		editDialog.add(center);
		HudContainer right = new HudContainer(name + "-right", 256, 600);
		right.setRelative();
		editDialog.add(right);
		
		saveButton = new HudButton("Save", HudManager.DEFAULT_FONT, (Texture2D)app.getAssetManager().loadTexture("Interface/button.png"), 80, 24);
		saveButton.setTextSize(24f);
		saveButton.setHorizontalAlignment(CENTERED, 0);
		saveButton.setVerticalAlignment(MAX_JUSTIFY, 12);
		saveButton.setListener(this);
		
		editDialog.setLayoutMode(HORIZONTAL_LIST);
		editDialog.setCentered();
		editWindow.add(editDialog);
		editWindow.add(saveButton);
		editWindow.setVisible(false);
		add(editWindow);
		
		HudContainer info = new HudContainer(name + "-info", 416, 96);
		info.setRelative();
		HudContainer iconHolder = new HudContainer(name + "-holder", 96, 96);
		iconHolder.setBorder(new Border(ColorRGBA.Black, 1));
		iconHolder.setRelative();
		icon = new HudComponent(name + "-info-icon", null, 96, 96);
		icon.setBackground(new ColorRGBA(1f, 1f, 1f, 1f));
		icon.setCentered();
		iconHolder.add(icon);
		info.add(iconHolder);
		HudContainer infoA = new HudContainer(name + "-info-a", 192, 96);
		infoA.setRelative();
		infoA.add(new HudLabel("ID:", 192, 24));
		idField = new HudComponent(name + "-info-id", null, 192, 24);
		idField.setText(new HudText(HudManager.DEFAULT_FONT, "", new ColorRGBA(0f, 0f, 0f, 1f)));
		idField.setBorder(new Border(ColorRGBA.Black, 1));
		idField.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		idField.getText().size = 24f;
		idField.getText().align = BitmapFont.Align.Left;
		infoA.add(idField);
		infoA.add(new HudLabel("Name:", 192, 24));
		nameField = new HudTextField(name + "-info-name", app.getInputManager(), 192, 24);
		infoA.add(nameField);
		infoA.setLayoutMode(VERTICAL_LIST);
		info.add(infoA);
		HudContainer infoB = new HudContainer(name + "-info-b", 96, 96);
		infoB.setRelative();
		infoB.add(new HudLabel("Category:", 128, 24));
		categoryField = new HudDropList(name + "-info-category", 128, 24);
		categoryField.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		categoryField.setListener(this);
		for (Schematic.Category c : Schematic.Category.values()) {
			categoryField.addItem(c.text());
		}
		infoB.add(categoryField);
		infoB.add(new HudLabel("Type:", 128, 24));
		typeField = new HudDropList(name + "-info-type", 128, 24);
		typeField.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		typeField.setListener(this);
		for (GameControl.ObjectType t : GameRegistry.getInstance().getObjectTypes()) {
			typeField.addItem(t.getName());
		}
		infoB.add(typeField);
		infoB.setLayoutMode(VERTICAL_LIST);
		info.add(infoB);
		left.add(info);
		info.setLayoutMode(HORIZONTAL_LIST);
		left.add(new HudLabel("Model:", 416, 24));
		modelList = new HudDropList(name + "-info-model", 416, 24);
		modelList.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		modelList.setListener(this);
		modelList.setRelative();
		modelList.registerWithInput(app.getInputManager());
		refreshModels(new File("libs/assets.jar"));
		left.add(modelList);
		left.add(new HudLabel("Description:", 416, 24));
		descriptField = new HudTextArea(name + "-info-description", app.getInputManager(), 416, 192);
		descriptField.setRelative();
		left.add(descriptField);
		left.setLayoutMode(VERTICAL_LIST);
		
		paramList = new HudContainer(name + "-params", 192, 0);
		paramList.setRelative();
		center.add(paramList);
		
		HudContainer resourcePanel = new HudContainer(name + "-resource-panel", 256, 24);
		resourcePanel.setRelative();
		resourcePanel.add(new HudLabel("Resources:", 96, 24));
		addResource = new HudButton("Add", HudManager.DEFAULT_FONT, (Texture2D)app.getAssetManager().loadTexture("Interface/button.png"), 80, 24);
		addResource.setTextSize(24f);
		addResource.setListener(this);
		addResource.setRelative();
		removeResource = new HudButton("Remove", HudManager.DEFAULT_FONT, (Texture2D)app.getAssetManager().loadTexture("Interface/button.png"), 80, 24);
		removeResource.setTextSize(24f);
		removeResource.setListener(this);
		removeResource.setRelative();
		resourcePanel.add(addResource);
		resourcePanel.add(removeResource);
		resourcePanel.setLayoutMode(HORIZONTAL_LIST);
		right.add(resourcePanel);
		resourceList = new HudItemList(name + "-resource-list", new ColorRGBA(0.92f, 0.92f, 0.92f, 1f), 256, 240, 24);
		resourceList.setRelative();
		resourceList.setListener(this);
		right.add(resourceList);
		right.setLayoutMode(VERTICAL_LIST);
		
		resourceWindow = new HudWindow(name + "-resource-window", (Texture2D)app.getAssetManager().loadTexture("Interface/border96.png"), 224, 192);
		resourceWindow.createNinePartTexture(32);
		resourceWindow.createExitButton((Texture2D)app.getAssetManager().loadTexture("Interface/exit_button.png"), 32, 32, 2, 2);
		resourceWindow.setCentered();		
		HudContainer resourceDialog = new HudContainer(name + "-resource-dialog", 192, 168);
		resourceDialog.setCentered();
		resourceDialog.add(new HudLabel("Description:", 192, 24));
		resourceDescription =new HudTextField(name + "-resource-descript", app.getInputManager(), 192, 24);
		resourceDescription.setRelative();
		resourceDialog.add(resourceDescription);
		resourceDialog.add(new HudLabel("Category:", 192, 24));
		resourceCategory = new HudDropList(name + "-resource-category", 192, 24);
		//resourceCategory.setItems(ResourceControl.Category.values());
		resourceCategory.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
		resourceCategory.setRelative();
		resourceDialog.add(resourceCategory);
		resourceDialog.add(new HudLabel("Quantity:", 192, 24));
		resourceQuantity = new HudTextField(name + "-resources-qty", app.getInputManager(), 192, 24);
		resourceQuantity.setRestriction(HudTextField.DIGITS_ONLY);
		resourceQuantity.setRelative();
		resourceDialog.add(resourceQuantity);
		resourceSave = new HudButton("Save Resource", HudManager.DEFAULT_FONT, (Texture2D)app.getAssetManager().loadTexture("Interface/button.png"), 192, 24);
		resourceSave.setTextSize(24f);
		resourceSave.setListener(this);
		resourceSave.setRelative();
		resourceDialog.add(resourceSave);
		resourceDialog.setLayoutMode(VERTICAL_LIST);
		resourceWindow.add(resourceDialog);
		resourceWindow.setVisible(false);
		add(resourceWindow);*/
	}
	public void show() {
		setVisible(true);
		schemaWindow.fadeIn(10f);
		notify(categoryList);
	}
	public void notify(HudComponent source) {
		if (source.equals(schemaWindow)) {
			setVisible(false);
			return;
		}
		if (source.equals(editWindow)) {
			schemaWindow.fadeIn(10f);
			return;
		}
		if (source.equals(modelList)) {
			locateIcon(new File("libs/assets.jar"));
			return;
		}
		if (source.equals(typeField)) {
			paramList.removeAll();
			int idx = 0;
			ArrayList<GameControl.Param> params = GameRegistry.findObjectType(typeField.getSelectedValue()).getParams();
			for (GameControl.Param p : params) {
				paramList.add(new HudLabel(p.getName() + ":", "", 192, 24));
				if (p.getParamClass().isEnum()) {
					/*HudDropList dropList = new HudDropList(name + "-param-field" + idx, 192, 24);
					dropList.setItems(p.getParamClass().getEnumConstants());
					dropList.setBackground(new ColorRGBA(0.92f, 0.92f, 0.92f, 1f));
					dropList.setRelative();
					paramList.add(dropList);*/
				//} else if (params[i].getRange() > 0) {
					//JSlider slider = new JSlider(params[i].getMin(), params[i].getMax());
					//paramFields[i] = slider;
				} else if (p.getParamClass() == Boolean.class) {
					/*HudCheckbox checkBox = new HudCheckbox(getName() + "-param-field" + idx, p.getName(), 192, 24);
					checkBox.setRelative();
					paramList.add(checkBox);*/
				} else {
					/*HudTextField textField =  new HudTextField(getName() + "-param-field" + idx, app.getInputManager(), 192, 24);
					if (p.getParamClass() == Integer.class || p.getParamClass() == Float.class) {
						textField.setRestriction(HudTextField.DIGITS_ONLY);
					}
					textField.setRelative();
					paramList.add(textField);*/
				}
				idx++;
			}
			paramList.setLayoutMode(VERTICAL_LIST);
			paramList.doLayout(paramList.getParent().getWidth(), paramList.getParent().getHeight());
			return;
		}
		if (source.equals(categoryList)) {
			schemaList.removeAll();
			int sIndex = categoryList.getSelectedIndex();
			if (sIndex >= 0) {
				for (Schematic schema : GameRegistry.getInstance().getSchematics(Schematic.Category.values()[sIndex])) {
					schemaList.add(getSchemaItem(schema));
				}
				schemaList.setLayoutMode(GRID_LIST);
				schemaList.doLayout(schemaList.getParent().getWidth(), schemaList.getParent().getHeight());
			}
			return;
		}
		if (source.equals(addButton)) {
			setFields(new Schematic());
			schemaWindow.fadeOut(4f);
			editWindow.fadeIn(10f);
			addButton.setSelected(false);
			return;
		}
		if (source.equals(saveButton)) {
			saveFields();
			if (editValue.getId() == 0) {
				GameRegistry.getInstance().add(editValue);
				categoryList.setSelectedIndex(categoryField.getSelectedIndex());
				notify(categoryList);
			}
			//GameRegistry.getInstance().save();
			editWindow.fadeOut(4f);
			schemaWindow.fadeIn(10f);
			saveButton.setSelected(false);
			return;
		}
		if (source.equals(addResource)) {	
			if (editValue != null) {
				Schematic.ResourceParam resource = new Schematic.ResourceParam(ResourceControl.DEFAULT_TYPE, "Unspecified", 1, "Any Resource");
				resourceList.addItem(resource);
				editValue.addResource(resource);
				//showResourceDialog(resource);
			}
			addResource.setSelected(false);
			return;
		}
		if (source.equals(removeResource)) {
			if (editValue != null) {
				int selected = resourceList.getSelectedIndex();
				if (selected >= 0) {
					Schematic.ResourceParam resource = (Schematic.ResourceParam)resourceList.removeItem(selected);
					editValue.removeResource(resource);
				}
			}
			removeResource.setSelected(false);
			return;
		}
		if (source.equals(resourceList)) {
			Object item = resourceList.getSelectedItem();
			if (item instanceof Schematic.ResourceParam) {
				Schematic.ResourceParam resource = (Schematic.ResourceParam)item;
				resourceDescription.setTextValue(resource.getDescription());
				//resourceCategory.setSelectedIndex(resource.getCategory().ordinal());
				//resourceStyle.setSelectedIndex(resource.getStyle());
				resourceQuantity.setTextValue("" + resource.getQuantity());
				resourceWindow.fadeIn(10f);
			}
		}
		if (source.equals(resourceSave)) {
			Object item = resourceList.getSelectedItem();
			if (item instanceof Schematic.ResourceParam) {
				Schematic.ResourceParam resource = (Schematic.ResourceParam)item;
				resource.description = resourceDescription.getTextValue();
				//resource.category = ResourceControl.Category.values()[resourceCategory.getSelectedIndex()];
				//resource.style = (short)resourceStyle.getSelectedIndex();
				resource.quantity = Short.valueOf(resourceQuantity.getTextValue());
			}
			resourceWindow.fadeOut(4f);
			resourceSave.setSelected(false);
			resourceList.updateList();
			return;
		}
		if (source instanceof HudButton) {
			Schematic schema = GameRegistry.getInstance().get(source.getName());
			if (schema != null) {
				setFields(schema);
				schemaWindow.fadeOut(4f);
				editWindow.fadeIn(10f);
			}
			((HudButton)source).setSelected(false);
			return;
		}
	}
	private HudComponent getSchemaItem(Schematic schema) {
		/*HudButton schemaIcon;
		try {
			Texture texture = app.getAssetManager().loadTexture(schema.getIcon());
			schemaIcon = new HudButton(schema.getName(), (Texture2D)texture, texture.getImage().getWidth(), texture.getImage().getHeight());
		} catch (Exception ex) { schemaIcon =  new HudButton(schema.getName(), null, null, 48, 48); }
		schemaIcon.setSwapCoords(false);
		schemaIcon.setClickCount(2);
		schemaIcon.setListener(this);
		schemaIcon.setSelectColor(new ColorRGBA(0.65f, 0.65f, 0.95f, 0.85f));*/
		HudContainer panel = new HudContainer("icon-holder-" + schema.getName(), 96, 96);
		panel.setRelative();
		//schemaIcon.setCentered();
		//panel.add(schemaIcon);
		return panel;
	}
	private void setFields(Schematic schema) {
		/*this.editValue = schema;
		if (editValue != null) {
			try {
				idField.getText().text = "";
				if (editValue.getId() != 0) {
					idField.getText().text = GameRegistry.getInstance().stringValue(editValue.getId());
				}
			} catch (Exception ex) {}
			
			nameField.setTextValue(editValue.getName());
			categoryField.setSelectedIndex(schema.getCategory().ordinal());
			typeField.setSelectedValue(editValue.getObjectType().getName());
			notify(typeField);
			modelList.setSelectedValue(editValue.getModel());
			notify(modelList);
			descriptField.setTextValue(editValue.getDescription());
			
			ArrayList<GameControl.Param> params = editValue.getObjectType().getParams();
			for (int p = 0; p < params.size(); p++) {
				HudComponent field = paramList.getChildAt(1+(p*2));
				if (field != null) {
					if (params.get(p).getParamClass().isEnum()) {
						((HudDropList)field).setSelectedIndex(editValue.getInt(p));
					//} else if (params[p].getRange() > 0) {
						//((JSlider)paramFields[p]).setValue(editValue.getInt(p));
					} else if (params.get(p).getParamClass() == Boolean.class) {
						((HudCheckbox)field).setSelected(editValue.getBoolean(p));
					} else {
						((HudTextField)field).setTextValue("" + editValue.get(p));
					}
				}
			}
			resourceList.removeAll();
			for (Schematic.ResourceParam resource : editValue.getRequiredResources()) {
				resourceList.addItem(resource);
			}
		}*/
	}
	private void saveFields() {
		/*editValue.setName(nameField.getTextValue());
		editValue.setObjectType(GameRegistry.findObjectType(typeField.getSelectedValue()));
		editValue.setCategory(Schematic.Category.values()[categoryField.getSelectedIndex()]);
		editValue.setModel(modelList.getSelectedValue());
		editValue.setDescription(descriptField.getTextValue());
		editValue.setIcon(icon.getName());
		
		ArrayList<GameControl.Param> params = editValue.getObjectType().getParams();
		for (int p = 0; p < params.size(); p++) {
			HudComponent field = paramList.getChildAt(1+(p*2));
			if (field != null) {
				if (params.get(p).getParamClass().isEnum()) {
					editValue.set(params.get(p), ((HudDropList)field).getSelectedIndex());
				//} else if (params[p].getRange() > 0) {
				//	editValue.set(params[p], ((JSlider)paramFields[p]).getValue());
				} else if (params.get(p).getParamClass() == Boolean.class) {
					editValue.set(params.get(p), ((HudCheckbox)field).isSelected());
				} else {
					editValue.set(params.get(p), ((HudTextField)field).getTextValue());
				}
			}
		}*/
	}
	private void locateIcon(File assetFile) {
		if (assetFile != null && assetFile.exists()) {
			String path = modelList.getSelectedValue().replace("Models", "Icons");
			path = path.replace(".j3o", "_Icon.png");
			if (path.length() == 0) {
				path = "Icons/null_icon.png";
			}
			ZipFile zip = null;
			try {
				zip = new ZipFile(assetFile);
				ZipEntry entry = zip.getEntry(path);
				if (entry != null) {
					System.out.println("Icon Found: " + path);
					Texture texture = app.getAssetManager().loadTexture(path);
					if (texture != null) {
						/*icon.setName(path);
						icon.setSize(texture.getImage().getWidth(), texture.getImage().getHeight());
						icon.setTexture((Texture2D)texture);
						icon.doLayout(96, 96);*/
					}
				/*Enumeration<? extends ZipEntry> entries = zip.entries();
				while ( entries.hasMoreElements()) {
					entry = entries.nextElement();
					String name = entry.getName();
					if (name.contains(path)) {
						if (name.contains("_icon")) {
							System.out.println("Icon Found: " + name + " | " + path);
							Texture texture = app.getAssetManager().loadTexture(name);
							if (texture != null) {
								icon.setName(name);
								icon.setSize(texture.getImage().getWidth(), texture.getImage().getHeight());
								icon.setTexture((Texture2D)texture);
								icon.doLayout(96, 96);
							}
							break;
						}
					}*/
				}
				zip.close();
			} catch (Exception ex) {
			} finally {
				try { zip.close(); }
				catch (Exception ex) {}
			}
		}
	}
	private void refreshModels(File assetFile) {
		if (assetFile != null && assetFile.exists()) {
			ZipFile zip = null;
			modelList.clear();
			modelList.addItem("Box");
			//modelList.addItem("VoxelShape");
			try {
				zip = new ZipFile(assetFile);
				ZipEntry entry;
				Enumeration<? extends ZipEntry> entries = zip.entries();
				while ( entries.hasMoreElements() )
				{
					entry = entries.nextElement();
					String name = entry.getName();
					if (name.contains("Models"))
					{
						if (name.endsWith("j3o") || name.endsWith("mesh.xml") || name.endsWith("obj"))
						{
							modelList.addItem(name);
						}
					}
				}
				zip.close();
			} catch (Exception ex) {
			} finally {
				try { zip.close(); }
				catch (Exception ex) {}
			}
		}
	}
}