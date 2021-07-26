package com.babble.api.controller;


import com.babble.api.request.room.RoomCreateReq;
import com.babble.api.request.room.RoomRelationReq;
import com.babble.api.request.user.UserLoginReq;
import com.babble.api.response.RoomRes;
import com.babble.api.service.*;
import com.babble.common.model.response.BaseResponseBody;
import com.babble.db.entity.*;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQuery;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Room 관련 API 요청 처리를 위한 컨트롤러 정의.
 */
@Api(value = "Room API", tags = {"Room"})
@RestController
@RequestMapping("/api/v1/room")
public class RoomController {

    @Autowired
    CategoryService categoryService;
    @Autowired
    HashtagService hashtagService;

    @Autowired
    UserService userService;
    @Autowired
    RoomHashtagService roomHashtagService;
    @Autowired
    RoomService roomService;

    @Autowired
    UserRoomService userRoomService;

    @PostMapping("/create")
    @ApiOperation(value = "방 생성", notes = "방에 대한 정보를 입력한다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 404, message = "사용자 없음"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    public ResponseEntity<? extends BaseResponseBody> roomCreate(
            @RequestBody @ApiParam(value="생성할 방 정보", required = true) RoomCreateReq roomCreateReq) {


            //방 생성 시 email, title, desc, thumbnail_url, category, hastag, speak 정보 넘어옴
            //category 테이블에서 category_name과 일치한 id 가져와 저장
            Category category =  categoryService.getCategoryByCategoryName(roomCreateReq.getName());
            //hostId는 현재 로그인된 유저 id
            User user = userService.getUserByUserEmail(roomCreateReq.getEmail());
            //isActivate default값 true

            //room create
            Room room = roomService.createRoom(roomCreateReq, user, category);

            //설정한 해시태그가 해시태그 테이블에 없을 경우, 추가 후 room_hashtag테이블에 roomId 와 hashtagId 함께 저장
            String [] tagList = roomCreateReq.getHashtag().split(" ");
            for(int i=0;i< tagList.length; i++) {
                Hashtag tag = hashtagService.getHashtagByHashtagName(tagList[i]);
                if (tag == null) { //해당 해시태그 없음 -> 해시태그 테이블에 넣고, 룸해시 테이블에 넣고
                    Hashtag hashtag = hashtagService.createHashtag(tagList[i]);
                    RoomHashtag roomHashtag = roomHashtagService.createRoomHashtag(hashtag, room);
                } else { //해당 해시태그 있을 경우
                    RoomHashtag roomHashtag = roomHashtagService.createRoomHashtag(tag, room);
                }
            }

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @PostMapping("/enter")
    @ApiOperation(value = "방 입장", notes = "방에 입장한다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 404, message = "사용자 없음"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    public ResponseEntity<? extends BaseResponseBody> roomEnter(
            @RequestBody @ApiParam(value="입장", required = true)RoomRelationReq roomRelationReq) {

            User user = userService.getUserByUserEmail(roomRelationReq.getEmail());
            Room room = roomService.getRoomByRoomTitle(roomRelationReq.getContent());
            UserRoom userRoom = userRoomService.createUserRoom(user, room);

        return ResponseEntity.status(200).body(BaseResponseBody.of(200, "Success"));
    }

    @GetMapping("/list")
    @ApiOperation(value = "방 정보", notes = "모든 방의 정보를 보여준다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "성공"),
            @ApiResponse(code = 401, message = "인증 실패"),
            @ApiResponse(code = 404, message = "사용자 없음"),
            @ApiResponse(code = 500, message = "서버 오류")
    })
    public ResponseEntity roomList() {

        List<RoomRes> result = new ArrayList<>();
        // 방이름, 썸네일 이미지, 카테고리, 태그, 시청자수
        // 먼저 방이름, 썸네일, 카테고리, 시청자수 가져오기

        QRoom qRoom =QRoom.room;
        QCategory qCategory = QCategory.category;
        QUserRoom qUserRoom = QUserRoom.userRoom;
        List<Tuple> roomInfo = roomService.getRoomInfo();
        for(int i=0;i<roomInfo.size();i++){

            String title = roomInfo.get(i).get(qRoom.title);
            String thumbnail = roomInfo.get(i).get(qRoom.thumbnailUrl);
            String category = roomInfo.get(i).get(qCategory.name);
            Long count = roomInfo.get(i).get(qUserRoom.room.id.count());

            List<String> hashtags = new ArrayList<>();
            List<Hashtag> list = roomHashtagService.findHashtagByRoomHashtagRoomId(roomInfo.get(i).get(qRoom.id));
            for(int j=0;j<list.size();j++){
                hashtags.add(list.get(j).getName());
            }

            RoomRes roomRes = new RoomRes();
            roomRes.setTitle(title);
            roomRes.setThumbnailUrl(thumbnail);
            roomRes.setCategory(category);
            roomRes.setViewers(count);
            roomRes.setHashtag(hashtags);

            result.add(roomRes);
        }
        return ResponseEntity.status(200).body(result);
    }
}
