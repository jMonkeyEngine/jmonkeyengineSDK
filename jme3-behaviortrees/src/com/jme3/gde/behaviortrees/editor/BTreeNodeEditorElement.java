/*
 * Copyright (c) 2009-2018 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.behaviortrees.editor;

import com.badlogic.gdx.ai.btree.BehaviorTree;
import com.badlogic.gdx.ai.btree.BranchTask;
import com.badlogic.gdx.ai.btree.Decorator;
import com.badlogic.gdx.ai.btree.LeafTask;
import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.branch.Sequence;
import com.badlogic.gdx.ai.btree.leaf.Failure;
import com.badlogic.gdx.ai.btree.leaf.Success;
import com.badlogic.gdx.ai.btree.leaf.Wait;
import com.badlogic.gdx.utils.Array;
import com.jme3.gde.behaviortrees.BTreeDataObject;
import com.jme3.gde.behaviortrees.BTreeMetaData;
import com.jme3.gde.behaviortrees.navigator.BTreeNavigatorPanel;
import com.jme3.gde.behaviortrees.navigator.nodes.TreeNodePanelNode;
import com.jme3.gde.behaviortrees.nodes.LeafTreeNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.RootNodePanel;
import com.jme3.gde.behaviortrees.nodes.SequentialNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.DecoratorNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.ParallelNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.SelectorNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.SequenceNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.BuiltinLeafTask;
import com.jme3.gde.core.editor.nodes.Connection;
import com.jme3.gde.core.editor.nodes.NodeEditor;
import com.jme3.gde.core.editor.nodes.Diagram;
import com.jme3.gde.core.editor.nodes.NodePanel;
import com.jme3.gde.core.editor.nodes.Selectable;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.errorreport.ExceptionUtils;
import java.awt.Dimension;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileEvent;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.lookup.InstanceContent;
import org.openide.windows.TopComponent;

// Register the NodeEditor
@MultiViewElement.Registration(
        displayName = "#LBL_BTREE_EDITOR",
        iconBase = "com/jme3/gde/core/editor/icons/matdef.png",
        mimeType = "text/gdx-ai-btree",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "BehaviorTree",
        position = 2000)
@Messages("LBL_BTREE_EDITOR=Nodes")
public final class BTreeNodeEditorElement extends JPanel implements 
        MultiViewElement, NodeEditor {
    protected BTreeDataObject obj;
    protected Lookup lkp;
    //private final MatDefEditorToolBar toolbar = new MatDefEditorToolBar();
    private transient MultiViewElementCallback callback;
    InstanceContent content;
    Selectable prevNode;
    BTreeMetaData metaData;

    @SuppressWarnings("LeakingThisInConstructor")
    public BTreeNodeEditorElement(final Lookup lkp) {
        content = new InstanceContent();
        this.lkp = lkp;
        initComponents();
        obj = lkp.lookup(BTreeDataObject.class);
        metaData = lkp.lookup(BTreeMetaData.class);
        if (obj == null) { // This happens when there was an error or maybe the object
            // has already been freed
            throw new IllegalArgumentException("Cannot build MatDefEditorlElement: obj null");
        }
        
        obj.setTopComponent(this);
        initDiagram(lkp, false);
        BTreeNavigatorPanel nav = new BTreeNavigatorPanel();
        obj.getLookupContents().add(nav);
        nav.updateData(obj);
        
        obj.getPrimaryFile().addFileChangeListener(new FileChangeAdapter() {
            @Override
            public void fileChanged(FileEvent fe) {
                refresh();
            }
        });
    }

    private void initDiagram(Lookup lkp, boolean saveOrphanedNodes) throws NumberFormatException {
        if (!obj.isLoaded()) {
            return;
        }
        
        List<TreeNodePanel> orphans = null;
        
        if (saveOrphanedNodes) {
            orphans = diagram1.getOrphanedNodes();
        }
        
        diagram1.clear();
        diagram1.setEditorParent(this);
        //diagram1.setPreferredSize(new Dimension(jScrollPane1.getWidth() - 2, jScrollPane1.getHeight() - 2));
        diagram1.setPreferredSize(new Dimension(-1, -1));
        
        BehaviorTree bTree = obj.getMainTree();
        RootNodePanel rnp = new RootNodePanel(diagram1, bTree);
        diagram1.addNode(rnp);
        obj.setRootNodePanel(rnp);
        
        if (bTree.getChildCount() > 1) {
            throw new IllegalStateException("There can only be one child of the rootNode!");
        }
        
        if (bTree.getChildCount() == 1) {
            TreeNodePanel childPanel = loadFromTree(rnp, bTree.getChild(0));
            diagram1.connect(rnp.getOutputByIndex(0), childPanel.getInputByIndex(0));
        }
        
        if (saveOrphanedNodes) {
            diagram1.restoreOrphanedNodes(orphans);
        }
        
        autoLayout();
        diagram1.setSize(diagram1.maxWidth, diagram1.getPreferredSize().height);
        diagram1.revalidate();        
        diagram1.repaint();
        jScrollPane1.addComponentListener(diagram1);
        jScrollPane1.setSize(diagram1.getSize());
        jScrollPane1.revalidate();
        jScrollPane1.repaint();
        // For some reason the scroll bars are only correct on the second time we're here
        /*diagram1.clear();
        JLabel error = new JLabel("<html><center>Cannot load material definition.<br>Please see the error log and fix it in the text editor</center></html>");
        error.setForeground(Color.ORANGE);
        error.setFont(new Font("Arial", Font.BOLD, 24));
        error.setBounds(0, 0, 400, 100);
        jScrollPane1.getHorizontalScrollBar().setValue(0);
        error.setLocation(Math.max(jScrollPane1.getViewport().getWidth() / 2 - 200, 0), Math.max(jScrollPane1.getViewport().getHeight() / 2 - 50, 0));
        diagram1.add(error);
        diagram1.repaint();*/
    }
    
    protected TreeNodePanel loadFromTree(TreeNodePanel parent, Task task) {
        TreeNodePanel panel;
        
        panel = taskToPanel(diagram1, task, null);
        diagram1.addNode(panel);
        
        for (int i = 0; i < task.getChildCount(); i++) {
            TreeNodePanel childPanel = loadFromTree(panel, task.getChild(i));
            if (panel instanceof SequentialNodePanel) {
                ((SequentialNodePanel)panel).setDynamic(false);
                diagram1.connect(panel.getOutputByIndex(i), childPanel.getInputByIndex(0));
                ((SequentialNodePanel)panel).setDynamic(true);
            } else if (panel instanceof DecoratorNodePanel) {
                if (i == 0) {
                    diagram1.connect(panel.getOutputByIndex(0), childPanel.getInputByIndex(0));
                } else {
                    throw new IllegalStateException("A Decorator cannot have more than 1 child");
                }
            } else {
                diagram1.connect(panel.createOutput(true), childPanel.getInputByIndex(0));
            }
        }
        
        return panel;
    }
    
    /**
     * Convert a BTree Task into a TreeNodePanel.
     * @param diagram1 The Diagram to attach it to.
     * @param task The Task
     * @param guard If this Panel represents a Guard Task or not
     * @return The Panel
     */
    public static TreeNodePanel taskToPanel(Diagram diagram1, Task task, TreeNodePanel guardedPanel) {
        TreeNodePanel panel;
        
        /* Handle the Panels from specific to generic: First specific implementations
         * of BranchTask (Parallel) and then catch-all. Same with Wait vs LeafTask
        */
        
        if (task instanceof Sequence) {
            // for the following tasks: + 1 so that the user can add new paths
            panel = new SequenceNodePanel(diagram1, task, task.getChildCount() + 1);
        } else if (task instanceof Selector) {
            panel = new SelectorNodePanel(diagram1, task, task.getChildCount() + 1);
        } else if (task instanceof Parallel) {
            panel = new ParallelNodePanel(diagram1, task, task.getChildCount() + 1);
        } else if (BranchTask.class.isAssignableFrom(task.getClass())) {
            panel = new SequentialNodePanel(diagram1, task, task.getChildCount() + 1);
        } else if (Decorator.class.isAssignableFrom(task.getClass())) {
            panel = new DecoratorNodePanel(diagram1, task);
        } else if (task instanceof Wait || task instanceof Success ||
                task instanceof Failure) {
            // Unfortunately this could come out of sync with libgdx some day,
            // but isAssignableFrom also triggers usercode tasks
            panel = new BuiltinLeafTask(diagram1, task);
        } else {
            if (guardedPanel != null) {
                panel = new LeafTreeNodePanel(diagram1, task, task.getClass().getPackage().getName(), task.getClass().getSimpleName(), guardedPanel);
            } else {
                panel = new LeafTreeNodePanel(diagram1, task, task.getClass().getPackage().getName(), task.getClass().getSimpleName());
            }
        }
        
        return panel;
    }

    /*public void switchTechnique(TechniqueBlock tech) {
        obj.getEditableFile().setCurrentTechnique(tech);        
        reload(obj.getEditableFile(), obj.getLookup());
    }*/

    public Diagram getDiagram() {
        return diagram1;
    }

    @Override
    public String getName() {
        return "MatDefVisualElement";
    }

    /**
     * Called FROM the Diagram when something has been selected and that change
     * has to be propagated to the Navigtor View. Unfortunately this loses the
     * capability of multiple selections atm.
     * @param selectable 
     */
    @Override
    public void selectionChanged(Selectable selectable) {
        BTreeNavigatorPanel nav = obj.getLookup().lookup(BTreeNavigatorPanel.class);
        //It's possible that the navigator is null if it's collapsed in the ui.
        //In that case we early return to avoid further issues
        if (nav == null){
            return;
        }
        
        Node n = findNode(nav.getExplorerManager().getRootContext(), selectable);
        if (n == null) {
            n = nav.getExplorerManager().getRootContext();
        }
        prevNode = selectable;
        
        try {
            nav.getExplorerManager().setSelectedNodes(new Node[]{n});
        } catch (PropertyVetoException veto) {
            
        }
        //FIXME this is hackish, each time it's used it spits a warning in the log.
        //without this line selecting a node in the editor select it in
        //the navigator explorer but does not displays its property sheet.
        //the warning says to manipulate the MultiViewElement lookup, but it just voids the tree view
        callback.getTopComponent().setActivatedNodes(new Node[]{n});
        
        /* 
         * @TODO: Comment MeFisto94 11.12.2018 Right now it looks like it's the
         * opposite: The Properties show up, but the Navigator Selection isn't
         * set anymore.
         */
    }
    
    /**
     * Called FROM the Navigator when something has changed and we shall 
     * reflect that in the diagram
     * @param nodes 
     */
    public void selectionChanged(Node[] nodes) {
        diagram1.select("", false); // Trigger Clearing of Multi Select
        for (Node n: nodes) {
            diagram1.select(((TreeNodePanelNode)n).getPanel(), true);
        }
    }
    
    public void refreshNavigator(boolean dontRefreshTheDiagram) {
        if (!dontRefreshTheDiagram) {
            refreshDiagram();
        }
        
        BTreeNavigatorPanel nav = obj.getLookup().lookup(BTreeNavigatorPanel.class);
        nav.updateData(obj);
    }
    
    public void refreshDiagram() {
        // we need to keep/handle orphaned nodes.
        initDiagram(lkp, true);
        setSize(getPreferredSize());
    }

    public void refresh() {
        obj.refresh(); // Re-Parse the tree first
        refreshNavigator(false); // implicitly calls refreshDiagram(), as the nav
        // is built upon diagram nodes
    }

    public void setModified() {
        obj.setDirty();
    }

    public ProjectAssetManager getAssetManager() {
        return obj.getLookup().lookup(ProjectAssetManager.class);
    }

    private Node findNode(Node root, Selectable select) {
        if (!(select instanceof Selectable) || !(root instanceof TreeNodePanelNode)) {
            return null;
        }
        
        TreeNodePanelNode panelNode = (TreeNodePanelNode)root;
        
        if (panelNode.getPanel().equals(select)) {
            return panelNode;
        } else if (panelNode.getChildren() != Children.LEAF) {
            Node n;
            for (Node node: panelNode.getChildren().getNodes()) {
                n = findNode(node, select);
                if (n != null) {
                    return n;
                }
            }
        }
        
        return null;
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        diagram1 = new com.jme3.gde.behaviortrees.editor.TreeDiagram();

        jScrollPane1.setMinimumSize(new java.awt.Dimension(0, 0));
        jScrollPane1.setName(""); // NOI18N

        diagram1.setBackground(new java.awt.Color(29, 29, 29));

        javax.swing.GroupLayout diagram1Layout = new javax.swing.GroupLayout(diagram1);
        diagram1.setLayout(diagram1Layout);
        diagram1Layout.setHorizontalGroup(
            diagram1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 1591, Short.MAX_VALUE)
        );
        diagram1Layout.setVerticalGroup(
            diagram1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 782, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(diagram1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 470, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 333, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private com.jme3.gde.behaviortrees.editor.TreeDiagram diagram1;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return null; //@TODO: FIXME
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public UndoRedo getUndoRedo() {
        if (getLookup().lookup(MultiViewEditorElement.class) != null) {
            return obj.getLookup().lookup(MultiViewEditorElement.class).getUndoRedo();
        } else {
            return null;
        }
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
    private static Field getField(Class clazz, String fieldName)
        throws NoSuchFieldException {
    try {
      return clazz.getDeclaredField(fieldName);
    } catch (NoSuchFieldException e) {
      Class superClass = clazz.getSuperclass();
      if (superClass == null) {
        throw e;
      } else {
        return getField(superClass, fieldName);
      }
    }
  }

    @Override
    public void makeMapping(Connection conn) {
        TreeNodePanel start = (TreeNodePanel)conn.getStart().getNode();
        TreeNodePanel end = (TreeNodePanel)conn.getEnd().getNode();
        
        if (!(start.getTask() instanceof LeafTask)) {
            start.getTask().addChild(end.getTask());
        }
        
        setModified();
    }

    @Override
    public void notifyRemoveConnection(Connection conn) {
        TreeNodePanel start = (TreeNodePanel)conn.getStart().getNode();
        TreeNodePanel end = (TreeNodePanel)conn.getEnd().getNode();
        
        // Unfortunately we need reflection, just hope it works for every subclass
        // for now it works. start.getTask().removeChild(end.getTask());
        
        try {
            if (!(start.getTask() instanceof LeafTask)) {
                if (start.getTask() instanceof BehaviorTree) {
                    Field root = getField(BehaviorTree.class, "rootTask");
                    root.setAccessible(true);
                    root.set(start.getTask(), null);
                } else if (start.getTask() instanceof BranchTask) {
                    Field children = getField(BranchTask.class, "children");
                    children.setAccessible(true);
                    Array<Task> arr = (Array<Task>)children.get(start.getTask());
                    arr.removeValue(end.getTask(), true); // true -> == over .equals
                } else if (start.getTask() instanceof Decorator) {
                    Field child = getField(Decorator.class, "child");
                    child.setAccessible(true);
                    child.set(start.getTask(), null);
                }
            }
        } catch (Exception e) {
            ExceptionUtils.caughtException(e, "Unable to use Reflection based hacks"
                    + " to remove the connection in the behavior tree. Maybe"
                    + " something changed in the LibGDX-AI API?");
        }
        setModified();
    }

    public void autoLayout(){
        diagram1.autoLayout();
    }
    
    @Override
    public void notifyRemoveNode(NodePanel node) {
        /* We don't hook into remove/add nodes but instead handle that by the
         * connection (a node is just not in the tree when it's not child of
         * someone).
        */
    }
    
    public void onAttachGuard(TreeNodePanel guardedPanel, TreeNodePanel guard) {
        guardedPanel.getTask().setGuard(guard.getTask());
        setModified();
    }
    
    public void onDetachGuard(TreeNodePanel guardedPanel, TreeNodePanel guard) {
        guardedPanel.getTask().setGuard(null);
        setModified();
    }

    @Override
    public Point getPositionFromMetaData(String key, int defaultx, int defaulty) throws NumberFormatException {
        Point position = new Point();
        String pos = metaData.getProperty("blah" + "/" + key, defaultx + "," + defaulty);

        if (pos != null) {
            String[] s = pos.split(",");
            position.x = Integer.parseInt(s[0]);
            position.y = Integer.parseInt(s[1]);
        }
        return position;
    }

    @Override
    public void savePositionToMetaData(String key, int x, int y) throws NumberFormatException {
        metaData.setProperty("blah" + "/" + key, x + "," + y);
    }

    public void reload() {
        try {
            obj.getLookup().lookup(EditorCookie.class).saveDocument();
            /*obj.getEditableFile().load(obj.getLookup());
            reload(obj.getEditableFile(), obj.getLookup());*/
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public void componentOpened() { }

    @Override
    public void componentClosed() { }

    @Override
    public void componentShowing() { }

    @Override
    public void componentHidden() { }

    @Override
    public void componentActivated() { }

    @Override
    public void componentDeactivated() { }

    @Override
    public void notifyDefaultValueUpdated(String name, String value) {
    }
    
}
