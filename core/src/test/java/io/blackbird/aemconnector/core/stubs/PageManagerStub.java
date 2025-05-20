package io.blackbird.aemconnector.core.stubs;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.Revision;
import com.day.cq.wcm.api.Template;
import com.day.cq.wcm.api.WCMException;
import com.day.cq.wcm.api.msm.Blueprint;
import io.blackbird.aemconnector.core.utils.PathUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;

import javax.jcr.Node;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class PageManagerStub implements PageManager {

    private ResourceResolver resourceResolver;
    private PageManager pageManagerDelegate;

    public PageManagerStub(ResourceResolver resourceResolver, PageManager pageManagerDelegate) {
        this.resourceResolver = resourceResolver;
        this.pageManagerDelegate = pageManagerDelegate;
    }

    @Override
    public Page getPage(String s) {
        return pageManagerDelegate.getPage(s);
    }

    @Override
    public Page getContainingPage(Resource resource) {
        return null;
    }

    @Override
    public Page getContainingPage(String s) {
        return null;
    }

    @Override
    public Page create(String s, String s1, String s2, String s3) throws WCMException {
        return pageManagerDelegate.create(s, s1, s2, s3);
    }

    @Override
    public Page create(String s, String s1, String s2, String s3, boolean b) throws WCMException {
        return pageManagerDelegate.create(s, s1, s2, s3, b);
    }

    @Override
    public Page move(Page page, String s, String s1, boolean b, boolean b1, String[] strings) throws WCMException {
        return null;
    }

    @Override
    public Page move(Page page, String s, String s1, boolean b, boolean b1, String[] strings, String[] strings1) throws WCMException {
        return null;
    }

    @Override
    public Resource move(Resource resource, String s, String s1, boolean b, boolean b1, String[] strings) throws WCMException {
        return null;
    }

    @Override
    public Resource move(Resource resource, String s, String s1, boolean b, boolean b1, String[] strings, String[] strings1) throws WCMException {
        return null;
    }

    @Override
    public Resource override(CopyOptions copyOptions) throws WCMException {
        return null;
    }

    @Override
    public Resource copy(CopyOptions copyOptions) throws WCMException {
        Page sourcePage = copyOptions.page;
        String targetPath = copyOptions.destination;
        String parent = PathUtils.getParent(targetPath);
        String fileName = PathUtils.getName(targetPath);
        Page page = pageManagerDelegate.create(parent, fileName, StringUtils.EMPTY, fileName, true);

        return resourceResolver.getResource(page.getPath());
    }

    @Override
    public Page copy(Page page, String s, String s1, boolean b, boolean b1) throws WCMException {
        return null;
    }

    @Override
    public Page copy(Page page, String s, String s1, boolean b, boolean b1, boolean b2) throws WCMException {
        return null;
    }

    @Override
    public Resource copy(Resource resource, String s, String s1, boolean b, boolean b1) throws WCMException {
        return null;
    }

    @Override
    public Resource copy(Resource resource, String s, String s1, boolean b, boolean b1, boolean b2) throws WCMException {
        return null;
    }

    @Override
    public void delete(Page page, boolean b) throws WCMException {

    }

    @Override
    public void delete(Page page, boolean b, boolean b1) throws WCMException {

    }

    @Override
    public void delete(Resource resource, boolean b) throws WCMException {

    }

    @Override
    public void delete(Resource resource, boolean b, boolean b1) throws WCMException {

    }

    @Override
    public void delete(Resource resource, boolean b, boolean b1, boolean b2) throws WCMException {

    }

    @Override
    public void order(Page page, String s) throws WCMException {

    }

    @Override
    public void order(Page page, String s, boolean b) throws WCMException {

    }

    @Override
    public void order(Resource resource, String s) throws WCMException {

    }

    @Override
    public void order(Resource resource, String s, boolean b) throws WCMException {

    }

    @Override
    public Template getTemplate(String s) {
        return null;
    }

    @Override
    public Collection<Template> getTemplates(String s) {
        return Collections.emptyList();
    }

    @Override
    public Collection<Blueprint> getBlueprints(String s) {
        return Collections.emptyList();
    }

    @Override
    public Revision createRevision(Page page) throws WCMException {
        return null;
    }

    @Override
    public Revision createRevision(Page page, String s, String s1) throws WCMException {
        return null;
    }

    @Override
    public Collection<Revision> getRevisions(String s, Calendar calendar) throws WCMException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Revision> getRevisions(String s, Calendar calendar, boolean b) throws WCMException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Revision> getChildRevisions(String s, Calendar calendar) throws WCMException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Revision> getChildRevisions(String s, Calendar calendar, boolean b) throws WCMException {
        return Collections.emptyList();
    }

    @Override
    public Collection<Revision> getChildRevisions(String s, String s1, Calendar calendar) throws WCMException {
        return Collections.emptyList();
    }

    @Override
    public Page restore(String s, String s1) throws WCMException {
        return null;
    }

    @Override
    public Page restoreTree(String s, Calendar calendar) throws WCMException {
        return null;
    }

    @Override
    public Page restoreTree(String s, Calendar calendar, boolean b) throws WCMException {
        return null;
    }

    @Override
    public void touch(Node node, boolean b, Calendar calendar, boolean b1) throws WCMException {

    }
}
