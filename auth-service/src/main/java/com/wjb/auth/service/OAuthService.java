package com.wjb.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.wjb.auth.common.exception.ServiceException;
import com.wjb.auth.dto.LoginResponse;
import com.wjb.auth.dto.OAuthBindingVO;
import com.wjb.auth.dto.OAuthCallbackResponse;
import com.wjb.auth.entity.SysOauthBinding;
import com.wjb.auth.mapper.SysOauthBindingMapper;
import com.wjb.auth.oauth.OAuthProvider;
import com.wjb.auth.oauth.OAuthUser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OAuthService {

    private final Map<String, OAuthProvider> providers;
    private final OAuthStateService stateService;
    private final SysOauthBindingMapper bindingMapper;
    private final AuthService authService;

    public OAuthService(List<OAuthProvider> providerList, OAuthStateService stateService,
                        SysOauthBindingMapper bindingMapper, AuthService authService) {
        this.providers = providerList.stream()
                .collect(Collectors.toMap(OAuthProvider::provider, Function.identity()));
        this.stateService = stateService;
        this.bindingMapper = bindingMapper;
        this.authService = authService;
    }

    private OAuthProvider provider(String name) {
        OAuthProvider p = providers.get(name);
        if (p == null) {
            throw new ServiceException("不支持的登录方式:" + name);
        }
        return p;
    }

    /** 登录模式授权地址 */
    public String loginUrl(String provider) {
        return provider(provider).authorizeUrl(stateService.createLogin());
    }

    /** 绑定模式授权地址 */
    public String bindUrl(String provider, Long userId) {
        return provider(provider).authorizeUrl(stateService.createBind(userId));
    }

    /** 统一回调:据 state.mode 登录或绑定 */
    public OAuthCallbackResponse callback(String providerName, String code, String state) {
        OAuthStateService.StateData data = stateService.consume(state);
        OAuthUser u = provider(providerName).fetchUser(code);

        if ("login".equals(data.mode())) {
            SysOauthBinding binding = bindingMapper.selectOne(new LambdaQueryWrapper<SysOauthBinding>()
                    .eq(SysOauthBinding::getProvider, u.provider())
                    .eq(SysOauthBinding::getOpenId, u.openId()));
            if (binding == null) {
                throw new ServiceException("该 " + u.provider() + " 账号未绑定,请先用账号登录后在『账号绑定』中绑定");
            }
            LoginResponse lr = authService.loginByUserId(binding.getUserId());
            return new OAuthCallbackResponse("login", lr.getToken());
        }

        // bind 模式
        Long userId = data.userId();
        SysOauthBinding exists = bindingMapper.selectOne(new LambdaQueryWrapper<SysOauthBinding>()
                .eq(SysOauthBinding::getProvider, u.provider())
                .eq(SysOauthBinding::getOpenId, u.openId()));
        if (exists != null) {
            if (exists.getUserId().equals(userId)) {
                return new OAuthCallbackResponse("bind", null); // 已绑定到本人,幂等成功
            }
            throw new ServiceException("该 " + u.provider() + " 账号已被其他用户绑定");
        }
        Long dup = bindingMapper.selectCount(new LambdaQueryWrapper<SysOauthBinding>()
                .eq(SysOauthBinding::getUserId, userId)
                .eq(SysOauthBinding::getProvider, u.provider()));
        if (dup != null && dup > 0) {
            throw new ServiceException("你已绑定过 " + u.provider() + ",请先解绑");
        }
        bindingMapper.insert(new SysOauthBinding(userId, u.provider(), u.openId()));
        return new OAuthCallbackResponse("bind", null);
    }

    /** 当前用户绑定列表 */
    public List<OAuthBindingVO> listBindings(Long userId) {
        List<SysOauthBinding> list = bindingMapper.selectList(new LambdaQueryWrapper<SysOauthBinding>()
                .eq(SysOauthBinding::getUserId, userId));
        List<OAuthBindingVO> vos = new ArrayList<>();
        for (SysOauthBinding b : list) {
            vos.add(new OAuthBindingVO(b.getProvider(), b.getOpenId()));
        }
        return vos;
    }

    /** 解绑 */
    public void unbind(Long userId, String provider) {
        bindingMapper.delete(new LambdaQueryWrapper<SysOauthBinding>()
                .eq(SysOauthBinding::getUserId, userId)
                .eq(SysOauthBinding::getProvider, provider));
    }
}
