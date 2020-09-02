package org.junit.experimental.categories;

import java.util.ArrayList;
import java.util.List;
import org.junit.internal.Classes;
import org.junit.runner.FilterFactory;
import org.junit.runner.FilterFactoryParams;
import org.junit.runner.manipulation.Filter;

abstract class CategoryFilterFactory implements FilterFactory {
    /* access modifiers changed from: protected */
    public abstract Filter createFilter(List<Class<?>> list);

    CategoryFilterFactory() {
    }

    @Override // org.junit.runner.FilterFactory
    public Filter createFilter(FilterFactoryParams params) throws FilterFactory.FilterNotCreatedException {
        try {
            return createFilter(parseCategories(params.getArgs()));
        } catch (ClassNotFoundException e) {
            throw new FilterFactory.FilterNotCreatedException(e);
        }
    }

    private List<Class<?>> parseCategories(String categories) throws ClassNotFoundException {
        List<Class<?>> categoryClasses = new ArrayList<>();
        for (String category : categories.split(",")) {
            categoryClasses.add(Classes.getClass(category));
        }
        return categoryClasses;
    }
}
