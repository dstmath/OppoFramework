package com.color.widget;

import android.app.OppoThemeHelper;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import com.oppo.media.OppoMultimediaServiceDefine;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import oppo.content.res.OppoThemeResources;
import oppo.util.OppoMultiLauncherUtil;

public class ColorItemHelper {
    private static String bluetooth = OppoMultimediaServiceDefine.BLUETOOTH_PACKAGE_NAME;
    private static String email = "com.android.email";
    private static String excel = "com.microsoft.office.excel";
    private static int mColumnCounts = 4;
    private static int mRowCounts = 2;
    private static String message = "com.android.mms";
    private static String nfc = "com.android.nfc";
    private static String oppocommunity = "com.oppo.community";
    private static String ppt = "com.microsoft.office.powerpoint";
    private static String prefix = "com.tencent";
    private static String read1 = "com.oppo.book";
    private static String read2 = "com.oppo.reader";
    private static String video = "com.coloros.video";
    private static String word = "com.microsoft.office.word";
    private static String wps = "cn.wps.moffice_eng";

    private static ResolveInfo[][] ListToArray(List<ResolveInfo> resolveInfoList) {
        mRowCounts = (int) Math.min(Math.ceil(((double) resolveInfoList.size()) / ((double) mColumnCounts)), 2.0d);
        ResolveInfo[][] array = (ResolveInfo[][]) Array.newInstance(ResolveInfo.class, new int[]{mRowCounts, mColumnCounts});
        int start = 0;
        int end = mColumnCounts + 0;
        int i = 0;
        while (start < resolveInfoList.size() && i < mRowCounts) {
            List<ResolveInfo> l = resolveInfoList.subList(start, end < resolveInfoList.size() ? end : resolveInfoList.size());
            System.arraycopy(l.toArray(), 0, array[i], 0, l.size());
            start = end;
            end = start + mColumnCounts;
            i++;
        }
        return array;
    }

    public static void reSortResolveInfoList(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rmessage = null;
        ResolveInfo remail = null;
        ResolveInfo rbluetooth = null;
        ResolveInfo roppocommunity = null;
        ResolveInfo rnfc = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(message)) {
                rmessage = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(email)) {
                remail = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(bluetooth)) {
                rbluetooth = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(oppocommunity)) {
                roppocommunity = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(nfc)) {
                rnfc = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.contains(prefix)) {
                rtencent.add((ResolveInfo) resolveInfoList.get(i));
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rmessage);
        add(resolveInfoList, remail);
        add(resolveInfoList, rbluetooth);
        add(resolveInfoList, roppocommunity);
        add(resolveInfoList, rnfc);
        resolveInfoList.addAll(rtencent);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListForViewDocument(List<ResolveInfo> resolveInfoList, Intent intent) {
        String ppt1 = "application/vnd.ms-powerpoint";
        String ppt2 = "application/mspowerpoint";
        String doc1 = "application/msword";
        String doc2 = "application/vnd.ms-word";
        String text = "text/plain";
        String video = "video/*";
        String excel1 = "application/vnd.ms-excel";
        String excel2 = "application/msexcel";
        String pdf = "application/pdf";
        String mimeType = intent.getType();
        if (mimeType == null) {
            reSortResolveInfoList(resolveInfoList);
        } else if (mimeType.equals(ppt1) || mimeType.equals(ppt2)) {
            reSortResolveInfoListPPT(resolveInfoList);
        } else if (mimeType.equals(doc1) || mimeType.equals(doc2)) {
            reSortResolveInfoListDoc(resolveInfoList);
        } else if (mimeType.equals(excel1) || mimeType.equals(excel2)) {
            reSortResolveInfoListExe(resolveInfoList);
        } else if (mimeType.equals(pdf)) {
            reSortResolveInfoListPdf(resolveInfoList);
        } else if (mimeType.equals(video)) {
            reSortResolveInfoListVideo(resolveInfoList);
        } else if (mimeType.equals(text)) {
            reSortResolveInfoListTxt(resolveInfoList);
        } else {
            reSortResolveInfoList(resolveInfoList);
        }
    }

    public static void reSortResolveInfoListPPT(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rwps = null;
        ResolveInfo rppt = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(wps)) {
                rwps = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(ppt)) {
                rppt = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rwps);
        add(resolveInfoList, rppt);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListDoc(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rwps = null;
        ResolveInfo rword = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(wps)) {
                rwps = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(word)) {
                rword = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rwps);
        add(resolveInfoList, rword);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListExe(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rwps = null;
        ResolveInfo rexe = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(wps)) {
                rwps = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(excel)) {
                rexe = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rwps);
        add(resolveInfoList, rexe);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListPdf(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rread1 = null;
        ResolveInfo rread2 = null;
        ResolveInfo rwps = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(read1)) {
                rread1 = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(read2)) {
                rread2 = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(wps)) {
                rwps = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rread1);
        add(resolveInfoList, rread2);
        add(resolveInfoList, rwps);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListTxt(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rread1 = null;
        ResolveInfo rread2 = null;
        ResolveInfo rwps = null;
        ResolveInfo rword = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            String packageName = ((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName;
            if (packageName.equals(read1)) {
                rread1 = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(read2)) {
                rread2 = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(wps)) {
                rwps = (ResolveInfo) resolveInfoList.get(i);
            } else if (packageName.equals(word)) {
                rword = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rread1);
        add(resolveInfoList, rread2);
        add(resolveInfoList, rwps);
        add(resolveInfoList, rword);
        resolveInfoList.addAll(rest);
    }

    public static void reSortResolveInfoListVideo(List<ResolveInfo> resolveInfoList) {
        ResolveInfo rradio = null;
        List<ResolveInfo> afterSort = new ArrayList();
        List<ResolveInfo> rtencent = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName.equals(video)) {
                rradio = (ResolveInfo) resolveInfoList.get(i);
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        resolveInfoList.clear();
        add(resolveInfoList, rradio);
        resolveInfoList.addAll(rest);
    }

    private static void add(List<ResolveInfo> l, ResolveInfo r) {
        if (r != null) {
            l.add(r);
        }
    }

    public static ColorItem[][] getAppInfo(List<ResolveInfo> resolveInfoList, PackageManager mPm) {
        ResolveInfo[][] resolveInfoArray = ListToArray(resolveInfoList);
        ColorItem[][] mAppInfo = (ColorItem[][]) Array.newInstance(ColorItem.class, new int[]{mRowCounts, mColumnCounts});
        Integer[][] mAppIcons = (Integer[][]) Array.newInstance(Integer.class, new int[]{mRowCounts, mColumnCounts});
        String[][] mAppNames = (String[][]) Array.newInstance(String.class, new int[]{mRowCounts, mColumnCounts});
        for (int i = 0; i < resolveInfoArray.length; i++) {
            for (int j = 0; j < resolveInfoArray[i].length; j++) {
                if (resolveInfoArray[i][j] != null) {
                    mAppInfo[i][j] = new ColorItem();
                    ComponentInfo ci = null;
                    if (resolveInfoArray[i][j].isMultiApp) {
                        ci = getComponentInfo(resolveInfoArray[i][j]);
                        mAppInfo[i][j].setText(resolveInfoArray[i][j].loadLabel(mPm).toString() + mPm.getText(OppoThemeResources.OPPO_PACKAGE, 201590120, null));
                    } else {
                        mAppInfo[i][j].setText(resolveInfoArray[i][j].loadLabel(mPm).toString());
                    }
                    if (ci != null) {
                        String name = OppoMultiLauncherUtil.getInstance().getAliasByPackage(ci.packageName);
                        if (name != null) {
                            mAppInfo[i][j].setText(name);
                        }
                    }
                    mAppInfo[i][j].setIcon(oppoLoadIconForResolveInfo(resolveInfoArray[i][j], mPm));
                }
            }
        }
        return mAppInfo;
    }

    private static ComponentInfo getComponentInfo(ResolveInfo info) {
        if (info == null) {
            return null;
        }
        if (info.activityInfo != null) {
            return info.activityInfo;
        }
        if (info.serviceInfo != null) {
            return info.serviceInfo;
        }
        if (info.providerInfo != null) {
            return info.providerInfo;
        }
        return null;
    }

    static Drawable oppoLoadIconForResolveInfo(ResolveInfo ri, PackageManager mPm) {
        Drawable dr;
        if (!(ri.resolvePackageName == null || ri.icon == 0)) {
            if (ri.activityInfo.packageName == null || (ri.resolvePackageName.contains(ri.activityInfo.packageName) ^ 1) == 0) {
                dr = OppoThemeHelper.getDrawable(mPm, ri.activityInfo.packageName, ri.icon, ri.activityInfo.applicationInfo, null);
            } else {
                dr = OppoThemeHelper.getDrawable(mPm, ri.resolvePackageName, ri.icon, null, null);
            }
            if (dr != null) {
                return dr;
            }
        }
        int iconRes = ri.getIconResource();
        if (iconRes != 0) {
            dr = OppoThemeHelper.getDrawable(mPm, ri.activityInfo.packageName, iconRes, ri.activityInfo.applicationInfo, null);
            if (dr != null) {
                return dr;
            }
        }
        return ri.loadIcon(mPm);
    }

    public static void adjustPosition(List<ResolveInfo> resolveInfoList, List<String> priorPackage) {
        List<ResolveInfo> prior = new ArrayList();
        List<ResolveInfo> priorSort = new ArrayList();
        List<ResolveInfo> rest = new ArrayList();
        for (int i = 0; i < resolveInfoList.size(); i++) {
            if (priorPackage.contains(((ResolveInfo) resolveInfoList.get(i)).activityInfo.packageName)) {
                prior.add((ResolveInfo) resolveInfoList.get(i));
            } else {
                rest.add((ResolveInfo) resolveInfoList.get(i));
            }
        }
        for (int j = 0; j < priorPackage.size(); j++) {
            for (int k = 0; k < prior.size(); k++) {
                if (((String) priorPackage.get(j)).equals(((ResolveInfo) prior.get(k)).activityInfo.packageName)) {
                    priorSort.add((ResolveInfo) prior.get(k));
                }
            }
        }
        resolveInfoList.clear();
        resolveInfoList.addAll(priorSort);
        resolveInfoList.addAll(rest);
    }
}
