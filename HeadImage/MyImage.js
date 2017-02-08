import React, {Component, PropTypes} from 'react';
import {
    View,
    StyleSheet,
    Image,
    NativeModules,
} from 'react-native';

export default class MyImage extends Component {

    constructor(props) {
        super(props);
        this.state = {
            uri: null,
        };
    }

    static defaultProps = {
        uri: null,
    };

    static propTypes = {
        uri: PropTypes.string,
        imageStyle: PropTypes.oneOfType([PropTypes.number, PropTypes.object]),
    }

    async componentWillReceiveProps() {
        let isExists = await NativeModules.HeadImageModule.isImageExists();
        if (this.props.uri !== null) {
            this.setState({
                uri: this.props.uri
            });
        } else if (isExists) {
            this.setState({
                uri: await NativeModules.HeadImageModule.getImageUri()
            });
        } else {
            this.setState({
                uri: 'head_default'
            });
        }
    }

    render() {
        return (
            <Image source={{uri: this.state.uri}} style={this.props.imageStyle}/>
        );
    }

}