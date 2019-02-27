/*
 * Copyright 1999-2019 fclassroom Group.
 *
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
 */
package org.ifelse.vl;

import com.intellij.openapi.project.Project;
import org.ifelse.IEAppLoader;
import org.ifelse.RP;
import org.ifelse.model.MFlowPoint;
import org.ifelse.model.MProject;
import org.ifelse.utils.IconFactory;
import org.ifelse.utils.Log;
import org.ifelse.utils.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class VLDoc extends JPanel implements DropTargetListener, MouseListener, MouseMotionListener, KeyListener {

    List<VLItem> eles = new ArrayList<VLItem>();
    VLItem item_focus;

    VLLine line_new;

    VListener listener;

    public VLDoc(VListener listener) {

        this.listener = listener;
        new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this);


        addMouseListener(this);
        addMouseMotionListener(this);
        addKeyListener(this);

        setFocusable(true);

    }

    @Override
    public void paint(Graphics g) {

        /*
        if( !g.getFont().getName().equals("Dialog") )
        {
            repaint();
            return;
        }
        */

        Graphics2D g2d = (Graphics2D) g;
        Font font = new Font(Font.DIALOG, Font.BOLD, 16);
        g.setFont(font);
        g2d.translate(0, 0);
        g2d.setColor(Color.white);

        // Log.i("vldoc width:%d height:%d  font:%s",getWidth(),getHeight(),g.getFont().getName());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        for (VLItem item : eles)
            item.paint(g2d);


    }

    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {

    }

    @Override
    public void dragExit(DropTargetEvent dte) {

    }

    @Override
    public void drop(DropTargetDropEvent dtde) {

        try {

            Object value = dtde.getTransferable().getTransferData(DataFlavor.stringFlavor);
            Log.i("vldoc accept:%s", value);
            {
                dtde.dropComplete(true);
                dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);

            }
            if (value instanceof MFlowPoint) {

                MFlowPoint mFlowPoint = (MFlowPoint) value;

                MProject mProject = IEAppLoader.getMProject(listener.project());
                VLPoint point = new VLPoint();
                point.id = mProject.getSequenceStr( listener.project() );

                point.setImage(IconFactory.createImage(RP.Path.getIconPath(listener.project(), mFlowPoint.icon)));

                point.x = dtde.getLocation().x;
                point.y = dtde.getLocation().y;

                point.flow_point_id = mFlowPoint.id;


                point.mproperties = mFlowPoint.copyProperties();

                eles.add(point);


                point.setFocus(true);


                repaint();


                listener.onDataChanged();

            }


        } catch (Exception e) {

            e.printStackTrace();
            return;

        }

        Point p = new Point(dtde.getLocation().x - 20, dtde.getLocation().y - 20);


    }

    public List<VLItem> getElements() {
        return eles;
    }


    int off_x, off_y;

    @Override
    public void mouseDragged(MouseEvent e) {

        if (item_focus != null) {

            if (e.getModifiers() == InputEvent.BUTTON1_MASK) {

                if (item_focus instanceof VLPoint) {
                    item_focus.setXY(e.getX() - item_focus.width / 2, e.getY() - item_focus.height / 2);
                }

                repaint();
            } else {

                if (item_focus instanceof VLPoint) {

                    if( line_new == null ) {
                        line_new = new VLLine();

                        line_new.id_from = item_focus.id;
                        line_new.point_from = item_focus;

                        eles.add(line_new);
                    }

                    line_new.move_cursor = e.getPoint();
                    repaint();
                }

            }
        }

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {


        switch (e.getKeyCode()){

            case KeyEvent.VK_BACK_SPACE :

                if( item_focus != null  ) {



                    if( !(item_focus instanceof VLLine) )
                        for(int i=eles.size()-1;i>-1;i--){

                            if( eles.get(i) instanceof VLLine ){

                                VLLine line = (VLLine) eles.get(i) ;

                                if( line.point_from == item_focus || line.point_to == item_focus ){

                                    eles.remove(line);

                                }


                            }

                        }



                    eles.remove(item_focus);

                    listener.onRemoved(item_focus);

                    listener.onDataChanged();

                    repaint();


                }

                break;
            case KeyEvent.VK_C:{

                if( Util.isMac() ){
                    if(  !e.isMetaDown() ){
                        return;
                    }
                }
                else{
                    if(! e.isControlDown() ){
                        return;
                    }
                }

                IEAppLoader.copy_item = item_focus.clone();


                Log.i("copy item commond + c");

            }
            break;
            case KeyEvent.VK_V:{


                if( Util.isMac() ){
                    if(  !e.isMetaDown() ){
                        return;
                    }
                }
                else{
                    if(! e.isControlDown() ){
                        return;
                    }
                }

                if( IEAppLoader.copy_item != null ){


                    String id = IEAppLoader.getMProject(listener.project()).getSequenceStr(listener.project());

                    VLItem item = IEAppLoader.copy_item.clone();

                    item.id = id;

                    if( point_pressed != null ) {

                        item.x = point_pressed.x;
                        item.y = point_pressed.y;

                    }

                    eles.add(item);

                    onFocusChanged(item);

                    repaint();

                }


            }
            break;
            case KeyEvent.VK_I:{


                if( Util.isMac() ){
                    if(  !e.isMetaDown() ){
                        return;
                    }
                }
                else{
                    if(! e.isControlDown() ){
                        return;
                    }
                }



            }
            break;
            case KeyEvent.VK_UP:{

                if( Util.isMac() ){
                    if(  !e.isMetaDown() && !e.isAltDown() ){
                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX(),item_focus.getY()-1);
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }
                else{
                    if(! e.isControlDown() && !e.isAltDown() ){
                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX(),item_focus.getY()-1);
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }


                repaint();
            }
            break;
            case KeyEvent.VK_DOWN:{

                if( Util.isMac() ){
                    if(  !e.isMetaDown() && !e.isAltDown() ){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX(),item_focus.getY()+1);
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }
                else{

                    if(! e.isControlDown()  && !e.isAltDown()){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX(),item_focus.getY()+1);
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }
                repaint();

            }
            break;
            case KeyEvent.VK_LEFT:{

                if( Util.isMac() ){
                    if(  !e.isMetaDown() && !e.isAltDown() ){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX()-1,item_focus.getY());
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }
                else{

                    if(! e.isControlDown()  && !e.isAltDown()){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX()-1,item_focus.getY());
                            repaint();
                            listener.onDataChanged();
                        }
                        return;
                    }
                }


            }
            break;

            case KeyEvent.VK_RIGHT:{

                if( Util.isMac() ){
                    if(  !e.isMetaDown() && !e.isAltDown() ){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX()+1,item_focus.getY());
                            repaint();
                            listener.onDataChanged();
                        }

                        return;
                    }
                }
                else{

                    if(! e.isControlDown()  && !e.isAltDown()){

                        if( item_focus != null  ) {
                            item_focus.setXY(item_focus.getX()+1,item_focus.getY());
                            repaint();
                            listener.onDataChanged();
                        }

                        return;
                    }
                }


            }
            break;






        }




    }


    public static interface VListener {

        Project project();

        void onFocus(VLItem item_focus);

        void onDoubleClick(VLItem item_focus);

        void onRemoved(VLItem item_focus);

        void onDataChanged();


    }


    //mouse event


    @Override
    public void mouseClicked(MouseEvent e) {

    }


    Point point_pressed;


    @Override
    public void mousePressed(MouseEvent e) {


        point_pressed = e.getPoint();

        requestFocus();


        VLItem temp = getFocus(e.getPoint(), null);

        onFocusChanged(temp);

        repaint();

    }

    private void setFocus(VLItem focus) {
        if (item_focus != null)
            item_focus.setFocus(false);
        item_focus = focus;
        if (item_focus != null)
            item_focus.setFocus(true);

    }


    public void onFocusChanged(VLItem temp) {


        if (temp != null && item_focus == temp) {

            return;
        }
        setFocus(temp);
        if (item_focus != null && listener != null) {
            listener.onFocus(item_focus);
        } else if (item_focus == null) {

            listener.onFocus(null);

        }


    }

    public VLItem getFocus(Point p, VLItem except) {


        //Point fixp = new Point();

        for (int i = eles.size() - 1; i > -1; i--) {

            VLItem item = eles.get(i);

            if (item.isPointIn(p) && item != except) {

                return item;

            }
        }
        return null;

    }

    @Override
    public void mouseReleased(MouseEvent e) {

        if( item_focus != null && e.getModifiers() == InputEvent.BUTTON3_MASK )
        {

            if( line_new != null ){

                VLItem  item = getFocus(e.getPoint(),item_focus);

                if( item == null || item.isLine() ) {

                    eles.remove(line_new);
                }
                else{

                    line_new.id = IEAppLoader.getMProject(listener.project()).getSequenceStr(listener.project());
                    line_new.id_to = item.id;
                    line_new.point_to = item;
                    line_new.newDefProperty();
                    listener.onDataChanged();

                }


            }
            line_new = null;
            repaint();
            return;

        }

        if( e.getClickCount() == 2 ){


            if( item_focus.isLine() ){

                repaint();
                return;
            }

            if( item_focus != null && listener != null )
                listener.onDoubleClick( item_focus );
//            else if( RP.scale != 1.0f ) {
//
//                RP.zoom();
//                repaint();
//                return;
//            }


        }



    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }


}
