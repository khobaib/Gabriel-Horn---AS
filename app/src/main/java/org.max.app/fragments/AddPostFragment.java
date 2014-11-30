package org.max.app.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.max.app.R;
import org.max.app.adapter.NothingSelectedSpinnerAdapter;
import org.max.app.interfaces.OnDateOrTimSetListener;
import org.max.app.model.LocalUser;
import org.max.app.utility.FontUtils;
import org.max.app.utility.Fonts;
import org.max.app.utility.Utils;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

public class AddPostFragment extends Fragment
{
    private final String TAG = this.getClass().getSimpleName();

    private static final int CAMERA_REQ_CODE = 901;
    private static final int GALLERY_REQ_CODE = 902;
    private static final String[] monthArray = {
            "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct",
            "Nov", "Dec"
    };

    private static final int BUTTON_POSITIVE = -1;
    private static final int BUTTON_NEGATIVE = -2;

    private Uri mImageCaptureUri;
    private File picFile = null;

    private LinearLayout llImageHolder, llLinkHolder;
    private boolean isImageVisible = false;

    private Bitmap scaledBmp;

    private Button bCamera, bHLink, btnAddPost;
    private ImageView ivSelectedImage;
    private LinearLayout llDateLayout, llTimeLayout;
    private EditText edtCategory, edtTitle, edtDetails;
    private TextView txtLink;
    private Spinner spCategory;
    String[] catList = {"Offer", "Event", "Post"};

    private View v;
    private Activity activity;
    private TextView tvMonth, tvDay, tvYear, tvHour, tvMin, tvAmPM;
    private String postLink = "";
    private ProgressDialog pDialog;

    public static Fragment newInstance()
    {
        return new AddPostFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        v = inflater.inflate(R.layout.fragment_add_post, container, false);
        bCamera = (Button) v.findViewById(R.id.btnCamera);
        bCamera.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPicDialog();
            }
        });

        bHLink = (Button) v.findViewById(R.id.btnHyperLink);
        bHLink.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showAddHyperLinkDialog();
            }
        });

        ivSelectedImage = (ImageView) v.findViewById(R.id.ivToBePostedImage);
        ivSelectedImage.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                showPicRemovePrompt();
            }
        });

        txtLink = (TextView) v.findViewById(R.id.tvToBePostedlinks);

        btnAddPost = (Button) v.findViewById(R.id.btnAddPost);
        btnAddPost.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onPostButtonClicked();
            }
        });

        edtCategory = (EditText) v.findViewById(R.id.etCategoryAddPost);
        edtCategory.setOnTouchListener(new OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent arg1)
            {
                spCategory.performClick();
                return true;
            }
        });

        spCategory = (Spinner) v.findViewById(R.id.sp_catgory);
        spCategory.setOnItemSelectedListener(new OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id)
            {
                if (position > 0)
                {
                    edtCategory.setText(catList[position - 1]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0)
            {
            }
        });

        setSpinner();

        edtTitle = (EditText) v.findViewById(R.id.etTitleAddPost);
        edtDetails = (EditText) v.findViewById(R.id.etDetailsAddPost);
        tvMonth = (TextView) v.findViewById(R.id.tvMonthAddPost);
        tvDay = (TextView) v.findViewById(R.id.tvDateAddPost);
        tvYear = (TextView) v.findViewById(R.id.tvYearAddPost);
        tvHour = (TextView) v.findViewById(R.id.tvHourAddPost);
        tvMin = (TextView) v.findViewById(R.id.tvMinAddPost);
        tvAmPM = (TextView) v.findViewById(R.id.tvDayPosiAddPost);

        initDateAndTimePicker();

        llImageHolder = (LinearLayout) v.findViewById(R.id.llImageToPostHolderAddPost);
        isImageVisible = false;
        llImageHolder.setVisibility(View.GONE);

        llLinkHolder = (LinearLayout) v.findViewById(R.id.llLinkToPostHolderAddPost);
        llLinkHolder.setVisibility(View.GONE);

        llDateLayout = (LinearLayout) v.findViewById(R.id.llDateAddPost);
        llDateLayout.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                showDatePicker();

            }
        });

        llTimeLayout = (LinearLayout) v.findViewById(R.id.llTimeAddPost);
        llTimeLayout.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                showTimePicker();

            }
        });

        if (savedInstanceState != null)
        {
            picFile = (File) savedInstanceState.getSerializable("post_pic");
            isImageVisible = savedInstanceState.getBoolean("is_image_visible");
            if (isImageVisible)
                setImageInTheView();
        }

        FontUtils.getInstance().overrideFonts(v, Fonts.LIGHT);
        return v;
    }

    public void onPostButtonClicked()
    {
        boolean validationError = false;

        StringBuilder errorMessage = new StringBuilder(getResources().getString(R.string.error_intro));
        String catgory = edtCategory.getText().toString();
        if (catgory.equals(""))
        {
            errorMessage.append("Enter Category");
            validationError = true;
        }

        final String title = edtTitle.getText().toString();
        if (title.equals(""))
        {
            if (validationError)
                errorMessage.append(", ");
            validationError = true;
            errorMessage.append("Enter Title");
        }

        String details = edtDetails.getText().toString();
        if (details.equals(""))
        {
            if (validationError)
                errorMessage.append(" and ");
            validationError = true;
            errorMessage.append("Enter Deatils");

        }

        errorMessage.append(getResources().getString(R.string.error_end));
        Date date = calculateExpiryDate();
        if (!validationError)
        {
            pDialog = Utils.createProgressDialog(getActivity());
            pDialog.show();

            ParseObject parseObject = ParseObject.create("Post");
            parseObject.put("contents", details);
            parseObject.put("title", title);
            parseObject.put("category", catgory);
            parseObject.put("expiration", date);
            parseObject.put("author", ParseUser.getCurrentUser());
            parseObject.put("link", postLink);
            parseObject.put("appCompany", LocalUser.getInstance().getParentCompany());

            if (mImageCaptureUri != null)
            {
                byte[] data = new byte[(int) picFile.length()];
                FileInputStream fileInputStream;

                try
                {
                    fileInputStream = new FileInputStream(picFile);
                    fileInputStream.read(data);
                    ParseFile pFile = new ParseFile(picFile.getName(), data);
                    parseObject.put("image", pFile);
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }

            parseObject.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                    {
                        if (getActivity() != null)
                        {
                            Toast.makeText(getActivity(), "Successfully posted.", Toast.LENGTH_LONG).show();
                            getActivity().getSupportFragmentManager().popBackStack();

                            HashMap<String, String> params = new HashMap<String, String>();
                            params.put("appIdentifier", getActivity().getPackageName());
                            ParseCloud.callFunctionInBackground("sendPush", params, new FunctionCallback<Object>()
                            {
                                @Override
                                public void done(Object o, ParseException e)
                                {
                                    if (e != null)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }
                    }
                    else
                    {
                        Log.e("TAG", "error");
                    }

                    pDialog.dismiss();
                }
            });

        }
        else
        {
            toast(errorMessage.toString());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        menu.clear();
        inflater.inflate(R.menu.add_post_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    private void setSpinner()
    {
        ArrayAdapter<String> myAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item, Arrays.asList(catList));
        spCategory.setAdapter(new NothingSelectedSpinnerAdapter(myAdapter, R.layout.row_spinner_nothing_selected,
                getActivity()));
        myAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    }

    private void toast(String str)
    {
        Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        activity = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        if (picFile == null)
            setImageFile();
        outState.putSerializable("post_pic", picFile);
        outState.putBoolean("is_image_visible", isImageVisible);
    }

    @SuppressLint("SimpleDateFormat")
    private Date calculateExpiryDate()
    {
        String day = tvDay.getText().toString();
        String stringMonth = tvMonth.getText().toString();
        String year = tvYear.getText().toString();
        String minute = tvMin.getText().toString();

        int hour = Integer.valueOf(tvHour.getText().toString());
        String amPm = tvAmPM.getText().toString();

        if (amPm.equals("PM"))
        {
            hour = hour + 12;
        }

        String dateString = day + "-" + stringMonth + "-" + year + " " + hour + ":" + minute;
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy hh:mm");

        Date date = null;
        try
        {
            date = formatter.parse(dateString);
            Log.e("DATE", date.toString());
        }
        catch (java.text.ParseException e)
        {

            e.printStackTrace();
        }

        return date;

    }

    private void initDateAndTimePicker()
    {
        Time time = new Time();
        time.setToNow();
        int month = time.month;
        int day = time.monthDay;
        int year = time.year;
        int hour = time.hour;
        int min = time.minute;
        tvYear.setText("" + year);
        tvDay.setText("" + day);
        tvMonth.setText(monthArray[month]);
        if (hour >= 12)
        {
            tvHour.setText((hour - 12) + "");
            tvAmPM.setText("PM");
        }
        else
        {
            tvHour.setText(hour + "");
            tvAmPM.setText("AM");
        }
        tvMin.setText(min + "");

    }

    private String getTimeStamp()
    {
        Time time = new Time();
        time.setToNow();
        int month = time.month;
        int day = time.monthDay;
        int year = time.year;
        int hour = time.hour;
        int min = time.minute;

        return year + "_" + month + "_" + day + "_" + hour + "_" + min;
    }

    private void showTimePicker()
    {
        DialogFragment df = DateOrTimePickerFragment.getInstance(new OnDateOrTimSetListener()
        {
            @Override
            public void dateOrTimeSet(int hour, int minute, int ignored)
            {
                Log.d(TAG, "Hour: " + hour + ", minute: " + minute + ", ignored: " + ignored);
                if (hour >= 12)
                {
                    tvHour.setText((hour - 12) + "");
                    tvAmPM.setText("PM");
                }
                else
                {
                    tvHour.setText(hour + "");
                    tvAmPM.setText("AM");
                }
                tvMin.setText(minute + "");
            }
        }, DateOrTimePickerFragment.TIME_PICKER);
        df.show(((FragmentActivity) activity).getSupportFragmentManager(), "time_picker");
    }

    private void showDatePicker()
    {
        DialogFragment df = DateOrTimePickerFragment.getInstance(new OnDateOrTimSetListener()
        {
            @Override
            public void dateOrTimeSet(int year, int month, int day)
            {
                Log.d(TAG, "Year: " + year + ", month: " + month + ", day: " + day);
                tvYear.setText(year + "");
                tvMonth.setText(monthArray[month]);
                tvDay.setText(day + "");
            }
        }, DateOrTimePickerFragment.DATE_PICKER);
        df.show(((FragmentActivity) activity).getSupportFragmentManager(), "date_picker");
    }

    private void showPicRemovePrompt()
    {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_pic_remove);

        dialog.findViewById(R.id.btnRemoveDialogPicRemover).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                llImageHolder.setVisibility(View.GONE);
                isImageVisible = false;
                scaledBmp = null;
                if (picFile == null)
                    setImageFile();
                if (picFile.delete())
                    Log.e(TAG, "picFile is deleted from the SD card.");
                dialog.dismiss();
            }
        });
        dialog.findViewById(R.id.btnCancelDialogPicRemover).setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });

        Window window = dialog.getWindow();
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.7f;
        lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    private void addLink()
    {
        if (postLink.equals(""))
        {
            llLinkHolder.setVisibility(View.GONE);
        }
        else
        {
            llLinkHolder.setVisibility(View.VISIBLE);
            txtLink.setText(postLink);
        }
    }

    private void showPicDialog()
    {
        final Dialog dialog = new Dialog(activity, android.R.style.Theme_Translucent_NoTitleBar_Fullscreen);
        dialog.setContentView(R.layout.dialog_pic_taker);
        Button btnTakePic = (Button) dialog.findViewById(R.id.btnCameraDialogPicTaker);
        btnTakePic.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                takePicture();
                dialog.dismiss();
            }
        });
        Button btnChoosePic = (Button) dialog.findViewById(R.id.btnChooseDialogPicTaker);
        btnChoosePic.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                setImageFile();
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, GALLERY_REQ_CODE);
                dialog.dismiss();
            }
        });
        Button btnCancel = (Button) dialog.findViewById(R.id.btnCancelDialogPicTaker);
        btnCancel.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog.dismiss();
            }
        });
        // Center-focus the dialog
        Window window = dialog.getWindow();
        window.setLayout(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        window.setGravity(Gravity.BOTTOM);

        // The below code is EXTRA - to dim the parent view by 70% :D
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.dimAmount = 0.7f;
        lp.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        dialog.getWindow().setAttributes(lp);
        dialog.show();
    }

    private void takePicture()
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try
        {
            setImageFile();

            // set uri from the file
            if (isSDCardMounted())
            {
                mImageCaptureUri = Uri.fromFile(picFile);
            }
            else
            {
                Toast.makeText(activity, "Media Not Mounted!", Toast.LENGTH_LONG).show();
                return;
            }
            intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
            // intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
            // mImageCaptureUri);
            intent.putExtra("return-data", true);
            Log.d(TAG, "cam intent starting");
            startActivityForResult(intent, CAMERA_REQ_CODE);
        }
        catch (ActivityNotFoundException e)
        {
            Log.d("Error", "Activity Not Found" + e.toString());
        }
    }

    private void setImageFile()
    {
        // Set the file name
        File directory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Gabriel Horn");
        directory.mkdir();

        String photoFileName = "post_pic" + getTimeStamp() + ".png";
        picFile = new File(directory.getPath(), photoFileName);
        try
        {
            picFile.createNewFile();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private int getCorrectionAngleForCam() throws IOException
    {
        ExifInterface exif = new ExifInterface(picFile.getPath());
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

        int angle = 0;
        if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
        {
            angle = 90;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
        {
            angle = 180;
        }
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
        {
            angle = 270;
        }

        return angle;
    }

    private void loadPicasaImageFromGallery(final Uri uri)
    {
        String[] projection = {MediaColumns.DATA, MediaColumns.DISPLAY_NAME};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(MediaColumns.DISPLAY_NAME);
            if (columnIndex != -1)
            {
                new Thread(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            Bitmap bitmap = android.provider.MediaStore.Images.Media.getBitmap(
                                    activity.getContentResolver(), uri);
                            scaledBmp = Bitmap.createScaledBitmap(bitmap, 200, 200, true);

                            activity.runOnUiThread(new Runnable()
                            {
                                @Override
                                public void run()
                                {
                                    try
                                    {
                                        FileOutputStream out = new FileOutputStream(picFile);
                                        scaledBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                                        mImageCaptureUri = Uri.fromFile(picFile);
                                        out.close();
                                        // ivProfilePic.setImageBitmap(scaledBmp);
                                        // btnUpdate.setVisibility(View.VISIBLE);
                                    }
                                    catch (FileNotFoundException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    catch (IOException e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                            });

                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }).start();
            }
        }
        cursor.close();

    }

    public String getPath(Uri uri)
    {
        String[] projection = {MediaColumns.DATA};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null)
        {
            cursor.moveToFirst();
            int columnIndex = cursor.getColumnIndexOrThrow(MediaColumns.DATA);
            String filePath = cursor.getString(columnIndex);
            cursor.close();
            return filePath;
        }
        else
            return uri.getPath();
    }

    private Bitmap decodeFile(File f, int imageQuality)
    {
        try
        {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, o);
            stream1.close();

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = imageQuality;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;

            while (true)
            {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            Log.i("SCALE", "scale = " + scale);

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, o2);
            stream2.close();
            return bitmap;
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private boolean isSDCardMounted()
    {
        String status = Environment.getExternalStorageState();

        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;
        return false;
    }

    public static void copyStream(InputStream input, OutputStream output) throws IOException
    {

        byte[] buffer = new byte[1024];
        int bytesRead;
        while ((bytesRead = input.read(buffer)) != -1)
        {
            output.write(buffer, 0, bytesRead);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (resultCode == Activity.RESULT_OK)
        {
            switch (requestCode)
            {
                case CAMERA_REQ_CODE:
                    Log.d(TAG, "cam onActivityResult");
                    try
                    {
                        if (picFile == null)
                            setImageFile();
                        // Log.e("msg",data.getData().toString());
                        Bitmap bmp = decodeFile(picFile, 500);
                        int angle = getCorrectionAngleForCam();
                        int w = bmp.getWidth();
                        if (w < 200)
                            w = 200;
                        int h = bmp.getHeight();
                        if (h < 200)
                            h = 200;
                        if (angle == 0)
                        {
                            scaledBmp = Bitmap.createScaledBitmap(bmp, w, h, true);
                        }
                        else
                        {
                            Matrix mat = new Matrix();
                            mat.postRotate(angle);
                            Bitmap correctBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), mat, true);
                            scaledBmp = Bitmap.createScaledBitmap(correctBmp, w, h, true);
                            Log.d("", "scaled");
                        }

                        try
                        {
                            FileOutputStream out = new FileOutputStream(picFile);
                            scaledBmp.compress(Bitmap.CompressFormat.PNG, 90, out);
                            mImageCaptureUri = Uri.fromFile(picFile);
                            out.close();
                        }
                        catch (Exception e)
                        {
                            Log.e("Error_Touhid", e.toString());
                        }
                    }
                    catch (IOException e)
                    {
                        Toast.makeText(activity, "IOException - Failed to load", Toast.LENGTH_SHORT).show();
                        Log.e("Camera", e.toString());
                    }
                    catch (OutOfMemoryError oom)
                    {
                        Toast.makeText(activity, "OOM error - Failed to load", Toast.LENGTH_SHORT).show();
                        Log.e("Camera", oom.toString());
                    }
                    Log.d(TAG, "starting crop");
                    saveImage();
                    break;

                case GALLERY_REQ_CODE:
                    try
                    {
                        mImageCaptureUri = data.getData();

                        if (getPath(mImageCaptureUri) != null)
                        {

                            InputStream inputStream = activity.getContentResolver().openInputStream(mImageCaptureUri);
                            FileOutputStream fileOutputStream = new FileOutputStream(picFile);
                            copyStream(inputStream, fileOutputStream);
                            fileOutputStream.close();
                            inputStream.close();

                            Log.d(TAG, "Gal code: starting crop");
                            saveImage();
                        }
                        else
                        {
                            System.out.println("Picasa Image!");
                            loadPicasaImageFromGallery(mImageCaptureUri);
                            saveImage();
                        }

                    }
                    catch (Exception e)
                    {
                        Log.e(TAG, "Error while creating thr picture file", e);
                        if (picFile == null)
                            Log.i(TAG, "<<<<<<<<<<<<<<<<<< Pic file is null !! >>>>>>>>>>>>>>>>>");
                    }
                    break;
            }
        }
    }

    private void saveImage()
    {
        scaledBmp = BitmapFactory.decodeFile(picFile.getPath());
        scaledBmp = Bitmap.createScaledBitmap(scaledBmp, 200, 200, true);
        setImageInTheView();
    }

    private void setImageInTheView()
    {
        llImageHolder.setVisibility(View.VISIBLE);
        isImageVisible = true;
        ImageView ivToPostimage = (ImageView) v.findViewById(R.id.ivToBePostedImage);
        ivToPostimage.setBackgroundResource(android.R.color.transparent);
        if (scaledBmp == null)
        {
            if (picFile == null)
                setImageFile();
            scaledBmp = decodeFile(picFile, 500);
            if (scaledBmp == null)
            {
                Log.e(TAG, "No image is found :o ... How dare you wanting to set it after removing it >:(");
                return;
            }
        }
        DisplayMetrics dm = getResources().getDisplayMetrics();
        int dw = dm.widthPixels;
        int iw = scaledBmp.getWidth();
        int ih = scaledBmp.getHeight();
        int nh = (int) (((float) dw / iw) * ih);
        scaledBmp = Bitmap.createScaledBitmap(scaledBmp, dw, nh, true);
        ivToPostimage.setImageBitmap(scaledBmp);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void showAddHyperLinkDialog()
    {
        AlertDialog addLinkDialog = new AlertDialog.Builder(getActivity()).create();
        addLinkDialog.setMessage("Enter or paste a link below:");

        final EditText etLink = new EditText(getActivity());
        etLink.setHint("URL (string with http://)");
        addLinkDialog.setView(etLink);
        addLinkDialog.setButton(BUTTON_POSITIVE, "Add Link", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                postLink = etLink.getText().toString();
                Log.d(TAG, "Link got: " + postLink);
                addLink();
                dialog.dismiss();
            }
        });

        addLinkDialog.setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int whichButton)
            {
                dialog.dismiss();
            }
        });
        addLinkDialog.show();
    }

}
