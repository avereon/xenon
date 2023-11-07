package com.avereon.xenon.workspace;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.paint.Paint;

public interface WorkareaSelectorItem {

	ObjectProperty<Paint> paintProperty();

	ObjectProperty<Node> graphicProperty();

	StringProperty textProperty();

}
