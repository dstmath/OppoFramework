package com.mediatek.server.cta;

import android.content.pm.PackageParser.Package;
import com.mediatek.cta.CtaPackageManagerInternal;

public class CtaPackageManagerInternalImpl extends CtaPackageManagerInternal {
    private CtaPermsController mCtaPermsController;

    public CtaPackageManagerInternalImpl(CtaPermsController ctaPermsController) {
        this.mCtaPermsController = ctaPermsController;
    }

    public void linkCtaPermissions(Package packageR) {
        if (this.mCtaPermsController != null) {
            this.mCtaPermsController.linkCtaPermissions(packageR);
        }
    }
}
