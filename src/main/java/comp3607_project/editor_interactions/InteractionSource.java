package comp3607_project.editor_interactions;

import java.io.Closeable;

public interface InteractionSource extends Closeable {

    
    void start();

    @Override
    void close();
}