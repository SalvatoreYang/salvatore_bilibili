package com.salvatore.bilibili.service;

import com.salvatore.bilibili.dao.VideoDao;
import com.salvatore.bilibili.domain.*;
import com.salvatore.bilibili.domain.exception.ConditionException;
import com.salvatore.bilibili.service.util.FastDFSUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VideoService {
    @Autowired
    private VideoDao videoDao;
    @Autowired
    private FastDFSUtil fastDFSUtil;
    @Autowired
    private UserCoinService userCoinService;
    @Autowired
    private UserService userService;

    @Transactional
    public void addVideos(Video video) {
        Date now = new Date();
        video.setCreateTime(now);
        video.setUpdateTime(now);
        videoDao.addVideos(video);
        Long videoId = video.getId();
        List<VideoTag> videoTagList = video.getVedioTagList();
        videoTagList.forEach(item -> {
            item.setCreateTime(now);
            item.setVideoId(videoId);
        });
        videoDao.batchAddVideoTags(videoTagList);
    }

    public PageResult<Video> pageListVideos(Integer size, Integer no, String area) {
        if (size == 0 || no == 0)
            throw new ConditionException("参数异常");
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("area", area);
        List<Video> list = new ArrayList<>();
        Integer total = videoDao.pageCountVideos(params);
        if (total > 0){
            list = videoDao.pageListVideos(params);
        }
        return new PageResult<>(total, list);
    }

    public void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url) throws Exception {
        fastDFSUtil.viewVideoOnlineBySlices(request, response, url);
    }

    public void addVideoLike(Long videoId, Long userId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null)
            throw new ConditionException("非法视频! ");
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        if (videoLike != null)
            throw new ConditionException("已经赞过! ");
        videoLike = new VideoLike();
        videoLike.setVideoId(videoId);
        videoLike.setUserId(userId);
        videoLike.setCreateTime(new Date());
        videoDao.addVideoLike(videoLike);
    }

    public void deleteVideoLike(Long videoId, Long userId) {
        videoDao.deleteVideoLike(videoId, userId);
    }

    public Map<String, Object> getVideoLikes(Long videoId, Long userId) {
        Long count = videoDao.getVideoLikes(videoId);
        VideoLike videoLike = videoDao.getVideoLikeByVideoIdAndUserId(videoId, userId);
        boolean like = videoLike != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("like", like);
        return result;
    }

    public void addVideoCollection(VideoCollection videoCollection, Long userId) {
        Long videoId = videoCollection.getVideoId();
        Long groupId = videoCollection.getGroupId();
        if (videoId == null || groupId == null)
            throw new ConditionException("参数异常! ");
        Video video = videoDao.getVideoById(videoId);
        if (video == null)
            throw new ConditionException("非法视频! ");
        // 删除原有视频收藏
        videoDao.deleteVideoCollection(videoId, userId);
        videoCollection.setUserId(userId);
        videoCollection.setCreateTime(new Date());
        // 添加新的视频收藏
        videoDao.addVideoCollection(videoCollection);
    }

    public void deleteVideoCollection(Long videoId, Long userId) {
        videoDao.deleteVideoCollection(videoId, userId);
    }

    public Map<String, Object> getVideoCollections(Long videoId, Long userId) {
        Long count = videoDao.getVideoCollections(videoId);
        VideoCollection videoCollection = videoDao.getVideoCollectionByVideoIdAndUserId(videoId, userId);
        boolean collect = videoCollection != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("collect", collect);
        return result;
    }

    @Transactional
    public void addVideoCoins(VideoCoin videoCoin, Long userId) {
        Long videoId = videoCoin.getVideoId();
        Integer amount = videoCoin.getAmount();
        if (videoId == null)
            throw new ConditionException("参数异常! ");
        Video video = videoDao.getVideoById(videoId);
        if (video == null)
            throw new ConditionException("非法视频! ");
        // 查询当前登陆用户是否拥有足够的硬币
        Integer userCoinsAmount = userCoinService.getUserCoinsAmount(userId);
        userCoinsAmount = userCoinsAmount == null ? 0 : userCoinsAmount;
        if (userCoinsAmount < amount)
            throw new ConditionException("硬币数量不足! ");
        // 查询当前登陆用户对该视频已经投了多少币
        VideoCoin dbVideoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        // 新增视频投币
        if (dbVideoCoin == null){
            videoCoin.setUserId(userId);
            Date now = new Date();
            videoCoin.setCreateTime(now);
            videoCoin.setUpdateTime(now);
            videoDao.addVideoCoin(videoCoin);
        }else {
            Integer dbAmount = dbVideoCoin.getAmount();
            dbAmount += amount;
            // 更新视频投币
            videoCoin.setUserId(userId);
            videoCoin.setAmount(dbAmount);
            videoCoin.setUpdateTime(new Date());
            videoDao.updateVideoCoin(videoCoin);
        }
        // 更新用户当前硬币总数
        userCoinService.updateUserCoinsAmount(userId, (userCoinsAmount - amount));
    }

    public Map<String, Object> getVideoCoins(Long videoId, Long userId) {
        Long count = videoDao.getVideoCoinsAmount(videoId);
        VideoCoin videoCoin = videoDao.getVideoCoinByVideoIdAndUserId(videoId, userId);
        boolean coin = videoCoin != null;
        Map<String, Object> result = new HashMap<>();
        result.put("count", count);
        result.put("coin", coin);
        return result;
    }

    public void addVideoComment(VideoComment videoComment, Long userId) {
        Long videoId = videoComment.getVideoId();
        if (videoId == null)
            throw new ConditionException("参数异常! ");
        Video video = videoDao.getVideoById(videoId);
        if (video == null)
            throw new ConditionException("非法视频! ");
        videoComment.setUserId(userId);
        videoComment.setCreateTime(new Date());
        videoDao.addVideoComment(videoComment);
    }

    public PageResult<VideoComment> pageListVideoComments(Integer size, Integer no, Long videoId) {
        Video video = videoDao.getVideoById(videoId);
        if (video == null)
            throw new ConditionException("非法视频! ");
        if (size == 0 || no == 0)
            throw new ConditionException("参数异常");
        Map<String, Object> params = new HashMap<>();
        params.put("start", (no - 1) * size);
        params.put("limit", size);
        params.put("videoId", videoId);
        Integer total = videoDao.pageCountVideoComments(params);
        List<VideoComment> list = new ArrayList<>();
        if (total > 0){
            list = videoDao.pageListVideoComments(params);
            // 批量查询二级评论
            List<Long> parentIdList = list.stream().map(VideoComment::getId).collect(Collectors.toList());
            List<VideoComment> childCommentList = videoDao.batchGetVideoCommentsByRootIds(parentIdList);
            // 批量查询用户信息
            Set<Long> userIdSet = list.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            Set<Long> replyUserIdSet = childCommentList.stream().map(VideoComment::getUserId).collect(Collectors.toSet());
            userIdSet.addAll(replyUserIdSet);
            List<UserInfo> userInfoList = userService.batchGetUserInfoByUserIds(userIdSet);
            Map<Long, UserInfo> userInfoMap = userInfoList.stream().collect(Collectors.toMap(UserInfo::getUserId, userInfo -> userInfo));
            list.forEach(comment -> {
                Long id = comment.getId();
                Long userId = comment.getUserId();
                List<VideoComment> childList = new ArrayList<>();
                childCommentList.forEach(child -> {
                    if(id.equals(child.getRootId())){
                        child.setUserInfo(userInfoMap.get(child.getUserId()));
                        child.setReplyUserInfo(userInfoMap.get(child.getReplyUserId()));
                        childList.add(child);
                    }
                });
                comment.setUserInfo(userInfoMap.get(userId));
                comment.setChildList(childList);
            });
        }
        return new PageResult<>(total, list);
    }

    public Map<String, Object> getVideoDetails(Long videoId) {

        Video video = videoDao.getVideoDetails(videoId);
        Long userId = video.getUserId();
        User user = userService.getUserInfo(userId);
        UserInfo userInfo = user.getUserInfo();
        Map<String, Object> result = new HashMap<>();
        result.put("video", video);
        result.put("userInfo", userInfo);
        return result;
    }
}
