package vip.comic18.finder.runner;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.resource.ClassPathResource;
import cn.hutool.core.lang.Filter;
import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import org.jboss.logging.Logger;
import vip.comic18.finder.service.ComicService;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jiayao on 2021/3/23.
 */
@QuarkusMain
public class ComicRunner implements QuarkusApplication {
    @Inject
    Logger log;
    @Inject
    ComicService comicService;

    private List<String> comicHomePages = JSONUtil.toList(new ClassPathResource("downloadPath.json").readUtf8Str(), String.class);

    @Override
    public int run(String... args) {
        log.info("注意身体,适度看漫");
        if(ArrayUtil.contains(args, "-s")) {
            log.info("后台模式");
            Quarkus.waitForExit();
        }
        log.info("前台模式");
        comicHomePages.addAll(Arrays.stream(ArrayUtil.filter(args, (Filter<String>) arg -> StrUtil.contains(arg, "http"))).toList());
        if(CollUtil.isEmpty(comicHomePages)) {
            log.info("下载列表为空,终止任务");
            return 0;
        }
        comicHomePages.forEach(comicHomePage -> comicService.getComicInfo(comicHomePage).subscribe().with(body -> comicService.consume(comicHomePage, body)));
        while(!comicService.exit()) {
            ThreadUtil.sleep(2000L);
        }
        return 0;
    }
}
