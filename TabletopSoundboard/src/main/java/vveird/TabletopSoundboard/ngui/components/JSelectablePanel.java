package vveird.TabletopSoundboard.ngui.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import vveird.TabletopSoundboard.ngui.util.ColorScheme;

public class JSelectablePanel extends JPanel {

	private static final long serialVersionUID = -5709653658285821316L;
	
	private List<SelectionListener> selectTionListener = new ArrayList<>(1);
	
	MouseList ml = new MouseList();
	
	private List<JSelectablePanel> group = new LinkedList<>();

	boolean selected = false;
	
	boolean canDeselctSelf = true;

	public JSelectablePanel(boolean canDeselctSelf) {
		this.canDeselctSelf = canDeselctSelf;
	}

	public JSelectablePanel(String title, boolean canDeselctSelf) {
		this.canDeselctSelf = canDeselctSelf;
		this.setLayout(null);
		this.setBackground(ColorScheme.SIDE_BAR_BACKGROUND_COLOR);
		JLabel lblTitle = new JLabel(title);
		lblTitle.setOpaque(false);
		lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
		lblTitle.setForeground(Color.LIGHT_GRAY);
		lblTitle.setBounds(10, 0, 180, 40);
		this.add(lblTitle);
	}

	public JSelectablePanel() {
	}
	
	public boolean isSelected() {
		return selected;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		setBackground(this.selected ? ColorScheme.MAIN_BACKGROUND_COLOR : ColorScheme.SIDE_BAR_BACKGROUND_COLOR);
		for (SelectionListener selectionListener : selectTionListener) {
			selectionListener.selectionChanged(this, this.selected);
		}
	}
	
	@Override
	public Component add(Component comp) {
		comp.addMouseListener(ml);
		return super.add(comp);
	}
	
	@Override
	public void remove(Component comp) {
		comp.removeMouseListener(ml);
		super.remove(comp);
	}
	
	public boolean addSelectionListener(SelectionListener list) {
		return this.selectTionListener.add(list);
	}
	
	public boolean  removeSelectionListener(SelectionListener list) {
		return this.selectTionListener.remove(list);
	}
	
	class MouseList implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {}
		@Override
		public void mousePressed(MouseEvent e) {}
		@Override
		public void mouseReleased(MouseEvent e) {
			if(canDeselctSelf && (!selected) == false || !selected)
				setSelected(!selected);
			else 
				setSelected(selected);
		}
		@Override
		public void mouseEntered(MouseEvent e) {}
		@Override
		public void mouseExited(MouseEvent e) {}
		
	}
	
	public static interface SelectionListener {
		public void selectionChanged(JSelectablePanel source, boolean newValue);
	}
	
	public static class JSelectablePanelGroup implements SelectionListener {
		
		List<JSelectablePanel> panels = new ArrayList<>(3);
		
		private boolean updateingPeers = false;
		
		public void add(JSelectablePanel panel) {
			if(!panels.contains(panel)) {
				panels.add(panel);
				panel.addSelectionListener(this);
			}
		}
		
		public void remove(JSelectablePanel panel) {
			if(panels.contains(panel)) {
				panels.remove(panel);
				panel.removeSelectionListener(this);
			}
		}
		
		@Override
		public void selectionChanged(JSelectablePanel source, boolean newValue) {
			if(!updateingPeers && panels.contains(source) && newValue) {
				updateingPeers = true;
				for (JSelectablePanel panel : panels) {
					if(panel != source)
						panel.setSelected(false);
				}
				updateingPeers = false;
			}
		}
	}
}
