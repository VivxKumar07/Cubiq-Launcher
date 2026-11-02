package dev.horizon.android;

import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import java.io.FileNotFoundException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Android SAF adapter only. The allowlist, paths and file operations live in Rust. */
public final class HorizonDocumentsProvider extends DocumentsProvider {
    private static final String[] ROOT_COLUMNS = {
        "root_id", "document_id", "title", "flags", "mime_types", "icon"
    };
    private static final String[] DOCUMENT_COLUMNS = {
        "document_id", "_display_name", "mime_type", "flags", "_size", "last_modified"
    };
    private static native String nativeCall(String home, String operation, String id, String arg)
            throws FileNotFoundException;
    private static native int nativeOpen(String home, String id, String mode)
            throws FileNotFoundException;

    @Override public boolean onCreate() {
        System.loadLibrary("horizon_android");
        return true;
    }
    private String call(String operation, String id, String arg) throws FileNotFoundException {
        return nativeCall(getContext().getFilesDir().getAbsolutePath(), operation, id, arg);
    }
    private Cursor query(String operation, String id, String[] projection, String[] defaults)
            throws FileNotFoundException {
        String[] columns = projection == null ? defaults : projection;
        MatrixCursor cursor = new MatrixCursor(columns);
        try {
            JSONArray rows = new JSONArray(call(operation, id, ""));
            for (int i = 0; i < rows.length(); i++) {
                JSONObject data = rows.getJSONObject(i);
                MatrixCursor.RowBuilder row = cursor.newRow();
                for (String column : columns) {
                    Object value = data.opt(column);
                    if ("icon".equals(column)) value = getContext().getApplicationInfo().icon;
                    row.add(value == JSONObject.NULL ? null : value);
                }
            }
            return cursor;
        } catch (JSONException error) {
            cursor.close();
            throw new FileNotFoundException("Invalid Horizon document response");
        }
    }
    private String changed(String operation, String id, String arg) throws FileNotFoundException {
        String response = call(operation, id, arg);
        getContext().getContentResolver().notifyChange(
                DocumentsContract.buildRootsUri(getContext().getPackageName() + ".documents"), null);
        String parent = id.lastIndexOf('/') < 0 ? "horizon" : id.substring(0, id.lastIndexOf('/'));
        if ("create".equals(operation) || "mkdir".equals(operation)) parent = id;
        getContext().getContentResolver().notifyChange(DocumentsContract.buildChildDocumentsUri(
                getContext().getPackageName() + ".documents", parent), null);
        try { return new JSONArray("[" + response + "]").optString(0, null); }
        catch (JSONException error) { throw new FileNotFoundException("Invalid document ID"); }
    }
    @Override public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        return query("roots", "", projection, ROOT_COLUMNS);
    }
    @Override public Cursor queryDocument(String id, String[] projection) throws FileNotFoundException {
        return query("document", id, projection, DOCUMENT_COLUMNS);
    }
    @Override public Cursor queryChildDocuments(String id, String[] projection, String sortOrder)
            throws FileNotFoundException {
        Cursor cursor = query("children", id, projection, DOCUMENT_COLUMNS);
        Uri children = DocumentsContract.buildChildDocumentsUri(
                getContext().getPackageName() + ".documents", id);
        cursor.setNotificationUri(getContext().getContentResolver(), children);
        return cursor;
    }
    @Override public boolean isChildDocument(String parent, String child) {
        try { return "true".equals(call("child", parent, child)); }
        catch (FileNotFoundException error) { return false; }
    }
    @Override public DocumentsContract.Path findDocumentPath(String parent, String child)
            throws FileNotFoundException {
        try {
            JSONArray ids = new JSONArray(call("path", child, parent == null ? "" : parent));
            java.util.ArrayList<String> path = new java.util.ArrayList<>();
            for (int i = 0; i < ids.length(); i++) path.add(ids.getString(i));
            return new DocumentsContract.Path(parent == null ? "horizon" : null, path);
        } catch (JSONException error) { throw new FileNotFoundException("Invalid document path"); }
    }
    @Override public ParcelFileDescriptor openDocument(String id, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        if (signal != null) signal.throwIfCanceled();
        return ParcelFileDescriptor.adoptFd(nativeOpen(
                getContext().getFilesDir().getAbsolutePath(), id, mode));
    }
    @Override public String createDocument(String parent, String mime, String name)
            throws FileNotFoundException {
        return changed(DocumentsContract.Document.MIME_TYPE_DIR.equals(mime) ? "mkdir" : "create",
                parent, name);
    }
    @Override public String renameDocument(String id, String name) throws FileNotFoundException {
        return changed("rename", id, name);
    }
    @Override public void deleteDocument(String id) throws FileNotFoundException {
        changed("delete", id, "");
    }
}
