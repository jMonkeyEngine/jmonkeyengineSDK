/*
 *  Copyright (c) 2009-2018 jMonkeyEngine
 *  All rights reserved.
 * 
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are
 *  met:
 * 
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 
 *  * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 * 
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 *  "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 *  TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 *  PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 *  CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 *  EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 *  PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 *  PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 *  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 *  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.gde.behaviortrees.editor;

import com.badlogic.gdx.ai.btree.Task;
import com.badlogic.gdx.ai.btree.branch.Parallel;
import com.badlogic.gdx.ai.btree.branch.Selector;
import com.badlogic.gdx.ai.btree.decorator.Invert;
import com.jme3.gde.behaviortrees.dialog.AddNodeDialog;
import com.jme3.gde.behaviortrees.nodes.LeafTreeNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.DecoratorNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.ParallelNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.RootNodePanel;
import com.jme3.gde.behaviortrees.nodes.impl.SelectorNodePanel;
import com.jme3.gde.core.assets.ProjectAssetManager;
import com.jme3.gde.core.editor.nodes.Connection;
import com.jme3.gde.core.editor.nodes.Diagram;
import com.jme3.gde.core.editor.nodes.NodePanel;
import com.jme3.gde.core.editor.nodes.Selectable;
import com.jme3.gde.core.editor.icons.Icons;
import com.jme3.gde.core.editor.nodes.ConnectionEndpoint;
import com.jme3.gde.core.editor.nodes.DraggablePanel;
//import static com.jme3.gde.materialdefinition.editor.TreeNodePanel.NodeType;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.JMenuItem;

/**
 * The Diagram is the main canvas where all nodes {@link DraggablePanel} and
 * their connections {@link ConnectionEndpoint} {@link Connection} are added onto.
 * @author Nehon
 */
public class TreeDiagram extends Diagram implements ComponentListener {
    
    int maxWidth = 0;

    @SuppressWarnings("LeakingThisInConstructor")
    public TreeDiagram() {
        super();
        setBackground(new Color(35, 35, 35));
    }
    
    @Override
    protected boolean mouseLMBPrePressedEvent(MouseEvent e) {
        return false;
    }
    
    @Override
    protected void showEdit(NodePanel node) {
        /*if (node instanceof TreeNodePanel &&
                parent instanceof MatDefEditorlElement) {
            ((MatDefEditorlElement)parent).showShaderEditor(node.getName(), 
                    ((TreeNodePanel)node).getType(), 
                    ((TreeNodePanel)node).filePaths);
        }*/
    }

    @Override
    public String makeKeyForConnection(Connection con, Object obj) {
        return "KEY";//MaterialUtils.makeKey((MappingBlock)obj, currentTechniqueName);
    }

    @Override
    protected Selectable trySelect(String key) {
        /*for (ShaderOutBusPanel outBusPanel : outBuses) {
            if (outBusPanel.getKey().equals(key)) {
                return outBusPanel;
            }
        }*/
        
        return null;
    }
    
    @Override
    protected void createPopupMenu() {
        super.createPopupMenu();
        
        JMenuItem dialogItem = createMenuItem("Add Node", Icons.node);
        dialogItem.addActionListener((ActionEvent e) -> {
            AddNodeDialog d = new AddNodeDialog(null, true,
                    ((BTreeNodeEditorElement)parent).obj.getLookup()
                            .lookup(ProjectAssetManager.class), 
                    TreeDiagram.this, clickLoc);
            d.setLocationRelativeTo(null);
            d.setVisible(true);
        });
        contextMenu.add(dialogItem);
        contextMenu.add(createSeparator());
        
        JMenuItem nodeItem = createMenuItem("Parallel", Icons.node);
        nodeItem.addActionListener((ActionEvent e) -> {
            TreeNodePanel tnp = new ParallelNodePanel(TreeDiagram.this, new Parallel(), 1);
            addNode(tnp);
            tnp.setLocation(clickLoc);
            tnp.revalidate();
            repaint();
        });

        contextMenu.add(nodeItem);
        
        nodeItem = createMenuItem("Invert", Icons.node);
        nodeItem.addActionListener((ActionEvent e) -> {
            TreeNodePanel tnp = new DecoratorNodePanel(TreeDiagram.this, new Invert());
            addNode(tnp);
            tnp.setLocation(clickLoc);
            tnp.revalidate();
            repaint();
        });
        contextMenu.add(nodeItem);
        
        nodeItem = createMenuItem("Selector", Icons.node);
        nodeItem.addActionListener((ActionEvent e) -> {
            TreeNodePanel tnp = new SelectorNodePanel(TreeDiagram.this, new Selector(), 1);
            addNode(tnp);
            tnp.setLocation(clickLoc);
            tnp.revalidate();
            repaint();
        });

        contextMenu.add(nodeItem);
    }
    
    @Override
    public Connection connect(ConnectionEndpoint start, ConnectionEndpoint end) {
        Connection conn = new Connection(start, end);
        
        // see logic in TreeConnectionEndPoint#canConnect
        // Feature: The Direction is clear here so we can drag them from both sides.
        if (end != null && start != null) {
            if (start.getParamType() == ConnectionEndpoint.ParamType.Input && 
                    (end.getParamType() == ConnectionEndpoint.ParamType.Output ||
                        end.getParamType() == ConnectionEndpoint.ParamType.Both)) {
                conn = new Connection(end, start);
            }
            
            start.connect(conn);
            end.connect(conn);
            addConnection(conn);
            return conn;
        } else {
            return null;
        }
    }
    
    public Stream<Connection> getConnectionsFrom(ConnectionEndpoint start) {
        return connections.stream().filter(con -> con.getStart().equals(start));
    }
    
    public Stream<Connection> getConnectionsFrom(NodePanel pnl) {
        return connections.stream().filter(con -> con.getStart().getNode().equals(pnl));
    }
    
    public boolean hasConnectionFrom(ConnectionEndpoint start) {
        return getConnectionsFrom(start).findAny().isPresent();
    }

    @Override
    protected int calcMaxWidth() {
        return 0;
    }
    
    @Override
    protected int calcMaxHeight() {
        return 0;
    }
    
    int minWidth = 0;
    int minHeight = 0;

    @Override
    public void autoLayout() {
        RootNodePanel rnp = ((BTreeNodeEditorElement)getEditorParent()).obj.getRootNodePanel();
        List<NodePanel> npL = new ArrayList<>();
        List<Integer> maxWidths = new ArrayList<>();
        maxWidth = 0;
        int center = 1500; // couldn't get that one dynamically atm

        // Two passes: the first one calculates the maxWidth and the second one
        // splits everything into left and right
        // An Enhancement would be to set center as to the parent's position,
        // but that breaks with our whole "level" system
        for (int i = 0; i < 2; i++) {
            npL.add(rnp);
            int level = 0;
            
            while (!npL.isEmpty()) {
                List<NodePanel> tmpList = new ArrayList<>();
                tmpList.addAll(npL);
                int x_level = (i == 0 ? center : center - maxWidths.get(level) / 2);

                for (NodePanel pnl: tmpList) {
                    pnl.setLocation(x_level, level * 150);
                    npL.remove(pnl); // Remove this item
                    // but add all children
                    npL.addAll(
                        getConnectionsFrom(pnl)
                            .map(Connection::getEnd)
                            .map(ConnectionEndpoint::getNode)
                            .filter(p -> p instanceof NodePanel)
                            .map(p -> (NodePanel)p)
                            .collect(Collectors.toList())
                    );
                    
                    x_level += (pnl.getWidth() + 20);
                }
                
                if (i == 0) {
                    maxWidths.add(x_level);
                    maxWidth = Math.max(maxWidth, x_level);
                } else {
                    level++;
                }
            }
            
            if (i == 0) {
                center = maxWidth/2;
            }
        }
    }
    
    public List<NodePanel> getPanelsAt(int x, int y) {
        return nodes.stream()
                // Panels extend from their location with "size" size.
                .filter(n -> n.getLocation().x < x) // is not starting "right" of x
                .filter(n -> n.getLocation().y < y) // is not starting "above"  of y
                .filter(n -> n.getLocation().x + n.getSize().width > x)
                .filter(n -> n.getLocation().y + n.getSize().height > y)
                .collect(Collectors.toList());
    }

    public void refreshNavigator(boolean dontRefreshTheDiagram) {
        ((BTreeNodeEditorElement)getEditorParent()).refreshNavigator(dontRefreshTheDiagram);
    }
    
    public List<TreeNodePanel> getOrphanedNodes() {
        /* During internal diagram rebuilds, we need to save nodes which are not
         * yet hooked up into the tree, we hence call them orphaned nodes.
         */
        return nodes.stream()
            .map(n -> (TreeNodePanel)n)
            .filter(n -> ((TreeNodePanel)n).isOrphan())
            .collect(Collectors.toList());
    }
    
    public void restoreOrphanedNodes(List<TreeNodePanel> orphans) {
        orphans.forEach(o -> addNode(o));
    }

    @Override
    public void toggleUpdateThread(boolean on) {
    }
}
