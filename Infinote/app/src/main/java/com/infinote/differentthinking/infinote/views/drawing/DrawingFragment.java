package com.infinote.differentthinking.infinote.views.drawing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Toast;

import com.android.graphics.CanvasView;
import com.fangxu.allangleexpandablebutton.AllAngleExpandableButton;
import com.fangxu.allangleexpandablebutton.ButtonData;
import com.fangxu.allangleexpandablebutton.ButtonEventListener;
import com.infinote.differentthinking.infinote.R;
import com.infinote.differentthinking.infinote.utils.InfinoteProgressDialog;
import com.infinote.differentthinking.infinote.utils.SaveDialog;
import com.infinote.differentthinking.infinote.utils.TextPopup;
import com.infinote.differentthinking.infinote.views.list_notes.ListNotesActivity;
import com.infinote.differentthinking.infinote.views.drawing.base.DrawingContract;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DrawingFragment extends Fragment implements DrawingContract.View {
    private AlertDialog.Builder alertDialog;
    private DrawingContract.Presenter presenter;
    private Context context;
    private InfinoteProgressDialog progressDialog;

    private int colorScheme;

    private AllAngleExpandableButton colorsButton;
    private AllAngleExpandableButton figuresButton;
    private AllAngleExpandableButton brushButton;
    private AllAngleExpandableButton modeButton;
    private SeekBar strokeSeekBar;
    private de.hdodenhof.circleimageview.CircleImageView saveButton;
    private de.hdodenhof.circleimageview.CircleImageView textButton;
    private de.hdodenhof.circleimageview.CircleImageView hideButton;
    private de.hdodenhof.circleimageview.CircleImageView undoButton;
    private de.hdodenhof.circleimageview.CircleImageView redoButton;

    private TextPopup popup;
    private SaveDialog saveDialog;

    private int flag = 0;

    private android.support.percent.PercentRelativeLayout percentLayout;
    private CanvasView canvas;
    private boolean editMode = false;
    private String pictureId;
    public static DrawingFragment newInstance() {
        return new DrawingFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             final Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drawing, container, false);

        this.colorsButton = (AllAngleExpandableButton) view.findViewById(R.id.button_expandable);
        this.figuresButton = (AllAngleExpandableButton) view.findViewById(R.id.drawer_figures);
        this.brushButton = (AllAngleExpandableButton) view.findViewById(R.id.drawer_strokes);
        this.modeButton = (AllAngleExpandableButton) view.findViewById(R.id.drawer_mode);
        this.strokeSeekBar = (SeekBar) view.findViewById(R.id.stroke_width);
        this.saveButton = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.save_button);
        this.textButton = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.text_button);
        this.hideButton = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.hide_button);
        this.undoButton = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.undo_button);
        this.redoButton = (de.hdodenhof.circleimageview.CircleImageView) view.findViewById(R.id.redo_button);
        this.canvas = (CanvasView) view.findViewById(R.id.canvas);
        this.percentLayout = (android.support.percent.PercentRelativeLayout) view.findViewById(R.id.percent_layout);
        this.colorScheme = R.color.iron;

        hideButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (flag == 0) {
                    flag = 1;
                    percentLayout.setVisibility(PercentRelativeLayout.GONE);
                    hideButton.setImageResource(R.mipmap.arrow_down);
                }
                else {
                    flag = 0;
                    percentLayout.setVisibility(PercentRelativeLayout.VISIBLE);
                    hideButton.setImageResource(R.mipmap.arrow_up);
                }
            }
        });

        undoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                canvas.undo();
            }
        });

        redoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                canvas.redo();
            }
        });

        this.strokeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                canvas.setPaintStrokeWidth(progress / 3);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        this.createModeButton();
        this.createStrokeButton();
        this.createColorsButton();
        this.createFiguresButton();

        this.canvas.setDrawingCacheEnabled(true);
        final DrawingContract.View currentView = this;
        this.saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog = new SaveDialog();
                saveDialog.setParentView(currentView);
                saveDialog.setCanvas(canvas);
                saveDialog.setEditMode(editMode);
                saveDialog.setPictureId(pictureId);
                saveDialog.setPresenter(presenter);

                saveDialog.show(getFragmentManager(), "save_dialog");
            }
        });
        this.textButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popup = new TextPopup();
                popup.setParentView(currentView);

                canvas.drawBitmap(canvas.getBitmap());
                canvas.setText("");

                popup.show(getFragmentManager(), "text_popup");
            }
        });

        byte[] decodedString = this.getActivity().getIntent().getByteArrayExtra("ENCODED_IMAGE");
        if (decodedString != null) {
            this.editMode = true;
            this.pictureId = this.getActivity().getIntent().getStringExtra("ID");
            Bitmap bm = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            this.canvas.drawBitmap(bm);
        }

        return view;
    }

    @Override
    public void setCanvasText(String text) {
        canvas.setMode(CanvasView.Mode.TEXT);
        canvas.setText(text);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public void setPresenter(DrawingContract.Presenter presenter) {
        this.presenter = presenter;
    }

    public void setDialog(InfinoteProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    @Override
    public void showDialogForLoading() {
        this.progressDialog.showProgress("Loading...");
    }

    @Override
    public void dismissDialog() {
        this.progressDialog.dismissProgress();
    }

    @Override
    public void notifySuccessful() {
        Toast.makeText(this.context, getString(R.string.note_saved_successfully), Toast.LENGTH_SHORT)
                .show();
    }

    @Override
    public void notifyError(String errorMessage) {
        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG)
                .show();
    }

    @Override
    public void showListNotesActivity() {
        Intent intent = new Intent(this.context, ListNotesActivity.class);
        startActivity(intent);
    }

    private void createModeButton() {
        final List<ButtonData> buttonDatas = new ArrayList<>();

        ButtonData defaultButton = ButtonData.buildIconButton(context, R.mipmap.pen_icon, 0);
        defaultButton.setBackgroundColorId(context, colorScheme);
        ButtonData eraserButton = ButtonData.buildIconButton(context, R.mipmap.eraser_icon, 0);
        eraserButton.setBackgroundColorId(context, colorScheme);

        buttonDatas.add(defaultButton);
        buttonDatas.add(eraserButton);

        modeButton.setButtonDatas(buttonDatas);
        modeButton.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                switch (index) {
                    case 1:
                        canvas.setPaintStrokeColor(Color.rgb(0, 0, 0));
                        canvas.setPaintStrokeWidth(3F);
                        break;
                    case 2:
                        canvas.setPaintStrokeColor(canvas.getBaseColor());
                        canvas.setPaintStrokeWidth(24F);
                        break;
                }
            }

            @Override
            public void onExpand() {
            }

            @Override
            public void onCollapse() {

            }
        });
    }

    private void createStrokeButton() {
        final List<ButtonData> buttonDatas = new ArrayList<>();

        ButtonData strokeButton = ButtonData.buildIconButton(context, R.mipmap.brush_icon, 0);
        strokeButton.setBackgroundColorId(context, colorScheme);
        ButtonData fillButton = ButtonData.buildIconButton(context, R.mipmap.fill_icon, 0);
        fillButton.setBackgroundColorId(context, colorScheme);
        ButtonData fillAndStrokeButton = ButtonData.buildIconButton(context, R.mipmap.fillandstroke_icon, 0);
        fillAndStrokeButton.setBackgroundColorId(context, colorScheme);

        buttonDatas.add(strokeButton);
        buttonDatas.add(fillButton);
        buttonDatas.add(fillAndStrokeButton);

        brushButton.setButtonDatas(buttonDatas);
        brushButton.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                canvas.setMode(CanvasView.Mode.DRAW);
                switch (index) {
                    case 1:
                        canvas.setPaintStyle(Paint.Style.STROKE);
                        break;
                    case 2:
                        canvas.setPaintStyle(Paint.Style.FILL);
                        break;
                    case 3:
                        canvas.setPaintStyle(Paint.Style.FILL_AND_STROKE);
                        break;
                }
            }

            @Override
            public void onExpand() {
            }

            @Override
            public void onCollapse() {

            }
        });
    }

    private void createFiguresButton() {
        final List<ButtonData> buttonDatas = new ArrayList<>();
        ButtonData penButton = ButtonData.buildIconButton(context, R.mipmap.pencil_icon, 0);
        penButton.setBackgroundColorId(context, colorScheme);
        ButtonData lineButton = ButtonData.buildIconButton(context, R.mipmap.line_icon, 0);
        lineButton.setBackgroundColorId(context, colorScheme);
        ButtonData rectangleButton = ButtonData.buildIconButton(context, R.mipmap.rectangle_icon, 0);
        rectangleButton.setBackgroundColorId(context, colorScheme);
        ButtonData circleButton = ButtonData.buildIconButton(context, R.mipmap.circle_icon, 0);
        circleButton.setBackgroundColorId(context, colorScheme);
        ButtonData elipseButton = ButtonData.buildIconButton(context, R.mipmap.elipse_icon, 0);
        elipseButton.setBackgroundColorId(context, colorScheme);
        ButtonData curvedLineButton = ButtonData.buildIconButton(context, R.mipmap.curvedline_icon, 0);
        curvedLineButton.setBackgroundColorId(context, colorScheme);
        buttonDatas.add(penButton);
        buttonDatas.add(lineButton);
        buttonDatas.add(rectangleButton);
        buttonDatas.add(circleButton);
        buttonDatas.add(elipseButton);
        buttonDatas.add(curvedLineButton);

        figuresButton.setButtonDatas(buttonDatas);
        figuresButton.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                canvas.setMode(CanvasView.Mode.DRAW);
                switch (index) {
                    case 1:
                        canvas.setDrawer(CanvasView.Drawer.PEN);
                        break;
                    case 2:
                        canvas.setDrawer(CanvasView.Drawer.LINE);
                        break;
                    case 3:
                        canvas.setDrawer(CanvasView.Drawer.RECTANGLE);
                        break;
                    case 4:
                        canvas.setDrawer(CanvasView.Drawer.CIRCLE);
                        break;
                    case 5:
                        canvas.setDrawer(CanvasView.Drawer.ELLIPSE);
                        break;
                    case 6:
                        canvas.setDrawer(CanvasView.Drawer.QUADRATIC_BEZIER);
                        break;
                }
            }

            @Override
            public void onExpand() {
            }

            @Override
            public void onCollapse() {

            }
        });
    }

    private void createColorsButton() {
        final List<ButtonData> buttonDatas = new ArrayList<>();
        ButtonData mainButton = ButtonData.buildIconButton(context, R.drawable.ic_action_pallette, 0);
        mainButton.setBackgroundColorId(context, colorScheme);
        buttonDatas.add(mainButton);
        int[] colors = {R.color.red,
                       R.color.blue,
                       R.color.orange,
                       R.color.yellow,
                       R.color.light_blue,
                       R.color.purple,
                       R.color.green,
                       R.color.light_green,
                       R.color.pink};

        for (int color: colors) {
            ButtonData buttonData =  ButtonData.buildTextButton("");
            buttonData.setBackgroundColorId(context, color);
            buttonDatas.add(buttonData);
        }

        colorsButton.setButtonDatas(buttonDatas);
        colorsButton.setButtonEventListener(new ButtonEventListener() {
            @Override
            public void onButtonClicked(int index) {
                if(canvas.getMode() == CanvasView.Mode.TEXT) {
                    canvas.setMode(CanvasView.Mode.DRAW);
                }

                switch (index) {
                    case 1:  canvas.setPaintStrokeColor(Color.BLACK);
                        break;
                    case 2:  canvas.setPaintStrokeColor(Color.rgb(244, 67, 54));
                        break;
                    case 3:  canvas.setPaintStrokeColor(Color.rgb(33,150,243));
                        break;
                    case 4:  canvas.setPaintStrokeColor(Color.rgb(255, 165, 0));
                        break;
                    case 5:  canvas.setPaintStrokeColor(Color.rgb(255,235,59));
                        break;
                    case 6:  canvas.setPaintStrokeColor(Color.rgb(3,169,244));
                        break;
                    case 7:  canvas.setPaintStrokeColor(Color.rgb(156,39,176));
                        break;
                    case 8:  canvas.setPaintStrokeColor(Color.rgb(76,175,80));
                        break;
                    case 9:  canvas.setPaintStrokeColor(Color.rgb(139,195,74));
                        break;
                    case 10:  canvas.setPaintStrokeColor(Color.rgb(233,30,99));
                        break;
                }
            }

            @Override
            public void onExpand() {
            }

            @Override
            public void onCollapse() {

            }
        });
    }
}
