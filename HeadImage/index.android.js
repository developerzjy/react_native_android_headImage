import React, { Component } from 'react';
import {
    AppRegistry,
    StyleSheet,
    Text,
    TouchableOpacity,
    Image,
    View,
    NativeModules
} from 'react-native';
import MyImage from './MyImage'

export default class HeadImage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            headImageUri: null,
        };
    }

    render() {
        return (
            <View style={styles.container}>
                <TouchableOpacity onPress={this._clickImage.bind(this)}>
                    <MyImage uri={this.state.headImageUri} imageStyle={{width: 100,height: 100}}/>
                </TouchableOpacity>
            </View>
        );
    }

    async _clickImage() {
        this.setState({
            headImageUri: await NativeModules.HeadImageModule.callCamera() // 相机拍照
            // headImageUri: await NativeModules.HeadImageModule.callGallery() // 相册选择图片
        });
    }

    componentDidMount() {
        this.setState({
            code: this.props.code
        });
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
    },
});

AppRegistry.registerComponent('HeadImage', () => HeadImage);
